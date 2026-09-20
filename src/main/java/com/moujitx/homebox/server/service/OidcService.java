package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.LoginResponse;
import com.moujitx.homebox.server.dto.response.TestConnectionResponse;
import com.moujitx.homebox.server.entity.Role;
import com.moujitx.homebox.server.entity.User;
import com.moujitx.homebox.server.event.ConfigChangedEvent;
import com.moujitx.homebox.server.exception.OidcAuthenticationException;
import com.moujitx.homebox.server.repository.RoleRepository;
import com.moujitx.homebox.server.repository.UserRepository;
import com.moujitx.homebox.server.security.ClientType;
import com.moujitx.homebox.server.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OidcService {

    private static final String CONFIG_ENABLED = "oidc.enabled";
    private static final String CONFIG_ISSUER = "oidc.issuer";
    private static final String CONFIG_CLIENT_ID = "oidc.client-id";
    private static final String CONFIG_CLIENT_SECRET = "oidc.client-secret";
    private static final String CONFIG_SCOPES = "oidc.scopes";
    private static final String CONFIG_USERNAME_CLAIM = "oidc.username-claim";
    private static final String CONFIG_REDIRECT_URI = "oidc.redirect-uri";
    private static final String CONFIG_APP_REDIRECT_URI = "oidc.app-redirect-uri";
    private static final String CONFIG_AUTO_CREATE_USER = "oidc.auto-create-user";
    private static final String CONFIG_DEFAULT_ROLE = "oidc.default-role";
    private static final String CONFIG_DISABLE_PASSWORD_LOGIN = "oidc.disable-password-login";

    private static final String DEFAULT_SCOPES = "openid profile email";
    private static final String DEFAULT_USERNAME_CLAIM = "preferred_username";
    private static final String DEFAULT_ROLE = "member";

    private static final long DISCOVERY_CACHE_MILLIS = 10 * 60 * 1000L;
    private static final long STATE_TTL_MILLIS = 10 * 60 * 1000L;

    private final SystemConfigService systemConfigService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private final RestClient restClient = createRestClient();

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private volatile SecretKey stateKey;
    private volatile OidcDiscovery cachedDiscovery;
    private volatile long discoveryExpiresAt;
    private volatile JwtDecoder idTokenDecoder;
    private volatile String idTokenDecoderJwksUri;

    public boolean isEnabled() {
        return "true".equalsIgnoreCase(config(CONFIG_ENABLED, "false"));
    }

    /**
     * Whether username/password login must be rejected. Only takes effect when OIDC is enabled and
     * fully configured, so an incomplete client setup can never lock everyone out.
     */
    public boolean isPasswordLoginDisabled() {
        if (!isEnabled() || !"true".equalsIgnoreCase(config(CONFIG_DISABLE_PASSWORD_LOGIN, "false"))) {
            return false;
        }
        return !issuer().isBlank()
                && !config(CONFIG_CLIENT_ID, "").isBlank()
                && !config(CONFIG_REDIRECT_URI, "").isBlank();
    }

    public String buildAuthorizeUrl(ClientType clientType) {
        ensureEnabled();
        OidcDiscovery discovery = discover();

        return UriComponentsBuilder.fromUriString(discovery.authorizationEndpoint())
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId())
                .queryParam("redirect_uri", redirectUri(clientType))
                .queryParam("scope", scopes())
                .queryParam("state", generateState(clientType))
                .build()
                .encode()
                .toUriString();
    }

    public LoginResponse handleCallback(String code, String state) {
        ensureEnabled();

        // The client type travels inside the signed state so the token exchange always uses the
        // exact same redirect URI as the authorize request, and cannot be tampered with.
        ClientType clientType = validateState(state);

        OidcDiscovery discovery = discover();
        Map<String, Object> tokenResponse = exchangeCode(discovery, code, clientType);

        Object idTokenValue = tokenResponse.get("id_token");
        if (!(idTokenValue instanceof String idToken) || idToken.isBlank()) {
            throw new OidcAuthenticationException("OIDC provider did not return an id_token");
        }

        Jwt idTokenJwt = decodeIdToken(discovery, idToken);
        User user = resolveUser(extractUsername(idTokenJwt), idTokenJwt);

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().getName(), clientType);
        long expiresIn = jwtTokenProvider.getExpirationMillis(clientType) / 1000;
        log.info("OIDC login succeeded for user '{}' ({})", user.getUsername(), clientType.getValue());
        return new LoginResponse(token, expiresIn, false);
    }

    public TestConnectionResponse testConnection(Map<String, String> overrides) {
        String overrideIssuer = overrides == null ? null : overrides.get(CONFIG_ISSUER);
        if (overrideIssuer != null && !overrideIssuer.isBlank()) {
            String issuer = normalizeIssuer(overrideIssuer);
            try {
                fetchDiscoveryDocument(issuer);
                return new TestConnectionResponse(true, "OIDC discovery document loaded from " + issuer);
            } catch (Exception e) {
                log.warn("OIDC connection test failed: {}", e.getMessage());
                return new TestConnectionResponse(false, "OIDC connection failed: " + e.getMessage());
            }
        }

        if (!isEnabled() || issuer().isBlank()) {
            return new TestConnectionResponse(false, "OIDC login is not enabled or issuer is not configured");
        }

        try {
            OidcDiscovery discovery = discover();
            return new TestConnectionResponse(true, "OIDC discovery document loaded from " + discovery.issuer());
        } catch (Exception e) {
            log.warn("OIDC connection test failed: {}", e.getMessage());
            return new TestConnectionResponse(false, "OIDC connection failed: " + e.getMessage());
        }
    }

    @EventListener
    public void onConfigChanged(ConfigChangedEvent event) {
        if (!"oidc".equals(event.getGroup())) {
            return;
        }
        cachedDiscovery = null;
        discoveryExpiresAt = 0;
        idTokenDecoder = null;
        idTokenDecoderJwksUri = null;
    }

    private Map<String, Object> exchangeCode(OidcDiscovery discovery, String code, ClientType clientType) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri(clientType));
        form.add("client_id", clientId());

        String clientSecret = clientSecret();
        if (!clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }

        try {
            Map<String, Object> body = restClient.post()
                    .uri(discovery.tokenEndpoint())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            if (body == null) {
                throw new OidcAuthenticationException("OIDC token endpoint returned an empty response");
            }
            return body;
        } catch (RestClientException e) {
            log.warn("OIDC token exchange failed: {}", e.getMessage());
            throw new OidcAuthenticationException("Failed to exchange OIDC authorization code");
        }
    }

    private Jwt decodeIdToken(OidcDiscovery discovery, String idToken) {
        try {
            return decoder(discovery).decode(idToken);
        } catch (org.springframework.security.oauth2.jwt.JwtException | IllegalArgumentException e) {
            log.warn("Invalid OIDC id_token: {}", e.getMessage());
            throw new OidcAuthenticationException("Invalid OIDC id_token");
        }
    }

    private JwtDecoder decoder(OidcDiscovery discovery) {
        JwtDecoder decoder = idTokenDecoder;
        if (decoder != null && discovery.jwksUri().equals(idTokenDecoderJwksUri)) {
            return decoder;
        }

        synchronized (this) {
            if (idTokenDecoder == null || !discovery.jwksUri().equals(idTokenDecoderJwksUri)) {
                NimbusJwtDecoder nimbus = NimbusJwtDecoder.withJwkSetUri(discovery.jwksUri()).build();
                OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefaultWithIssuer(discovery.issuer()),
                        new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
                                audience -> audience != null && audience.contains(clientId()))
                );
                nimbus.setJwtValidator(validator);
                idTokenDecoder = nimbus;
                idTokenDecoderJwksUri = discovery.jwksUri();
            }
            return idTokenDecoder;
        }
    }

    private User resolveUser(String username, Jwt idToken) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            if (!"true".equalsIgnoreCase(config(CONFIG_AUTO_CREATE_USER, "false"))) {
                throw new OidcAuthenticationException("No HomeBox user is linked to OIDC account: " + username);
            }

            String roleName = config(CONFIG_DEFAULT_ROLE, DEFAULT_ROLE);
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new OidcAuthenticationException("Default OIDC role not found: " + roleName));

            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setDisplayName(displayNameFrom(idToken, username));
            user.setRole(role);
            user.setForceChangePassword(false);
            user.setOidcUser(true);
            log.info("Auto-created HomeBox user '{}' from OIDC login", username);
            return userRepository.save(user);
        });
    }

    private String extractUsername(Jwt idToken) {
        String configuredClaim = config(CONFIG_USERNAME_CLAIM, DEFAULT_USERNAME_CLAIM);
        String username = firstNonBlank(
                idToken.getClaimAsString(configuredClaim),
                idToken.getClaimAsString(DEFAULT_USERNAME_CLAIM),
                idToken.getClaimAsString("email"),
                idToken.getSubject()
        );
        if (username == null) {
            throw new OidcAuthenticationException("OIDC id_token does not contain a usable username claim");
        }
        return username;
    }

    private String displayNameFrom(Jwt idToken, String fallback) {
        String name = firstNonBlank(idToken.getClaimAsString("name"), idToken.getClaimAsString(DEFAULT_USERNAME_CLAIM));
        return name == null ? fallback : name;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private OidcDiscovery discover() {
        String issuer = issuer();
        if (issuer.isBlank()) {
            throw new OidcAuthenticationException("OIDC issuer is not configured");
        }

        OidcDiscovery cached = cachedDiscovery;
        if (cached != null && issuer.equals(cached.issuer()) && System.currentTimeMillis() < discoveryExpiresAt) {
            return cached;
        }

        synchronized (this) {
            if (cachedDiscovery != null && issuer.equals(cachedDiscovery.issuer())
                    && System.currentTimeMillis() < discoveryExpiresAt) {
                return cachedDiscovery;
            }

            Map<String, Object> document = fetchDiscoveryDocument(issuer);
            OidcDiscovery discovered = new OidcDiscovery(
                    issuer,
                    requireEndpoint(document, "authorization_endpoint"),
                    requireEndpoint(document, "token_endpoint"),
                    requireEndpoint(document, "jwks_uri")
            );
            cachedDiscovery = discovered;
            discoveryExpiresAt = System.currentTimeMillis() + DISCOVERY_CACHE_MILLIS;
            return discovered;
        }
    }

    private Map<String, Object> fetchDiscoveryDocument(String issuer) {
        String documentUrl = issuer + "/.well-known/openid-configuration";
        try {
            Map<String, Object> document = restClient.get()
                    .uri(documentUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            if (document == null) {
                throw new OidcAuthenticationException("OIDC discovery document is empty");
            }
            return document;
        } catch (RestClientException e) {
            log.warn("Failed to load OIDC discovery document from {}: {}", documentUrl, e.getMessage());
            throw new OidcAuthenticationException("Failed to load OIDC discovery document");
        }
    }

    private String requireEndpoint(Map<String, Object> document, String key) {
        Object value = document.get(key);
        if (!(value instanceof String endpoint) || endpoint.isBlank()) {
            throw new OidcAuthenticationException("OIDC discovery document is missing '" + key + "'");
        }
        return endpoint;
    }

    private String generateState(ClientType clientType) {
        Date now = new Date();
        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("clientType", clientType.getValue())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + STATE_TTL_MILLIS))
                .signWith(stateKey())
                .compact();
    }

    private ClientType validateState(String state) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(stateKey())
                    .build()
                    .parseSignedClaims(state)
                    .getPayload();
            return ClientType.fromValue(claims.get("clientType", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            throw new OidcAuthenticationException("Invalid or expired OIDC state");
        }
    }

    private SecretKey stateKey() {
        SecretKey key = stateKey;
        if (key != null) {
            return key;
        }
        synchronized (this) {
            if (stateKey == null) {
                stateKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            }
            return stateKey;
        }
    }

    private static RestClient createRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    private void ensureEnabled() {
        if (!isEnabled()) {
            throw new OidcAuthenticationException("OIDC login is not enabled");
        }
    }

    private String issuer() {
        return normalizeIssuer(config(CONFIG_ISSUER, ""));
    }

    private String normalizeIssuer(String value) {
        String issuer = value == null ? "" : value.trim();
        while (issuer.endsWith("/")) {
            issuer = issuer.substring(0, issuer.length() - 1);
        }
        return issuer;
    }

    private String clientId() {
        String clientId = config(CONFIG_CLIENT_ID, "");
        if (clientId.isBlank()) {
            throw new OidcAuthenticationException("OIDC client id is not configured");
        }
        return clientId;
    }

    private String clientSecret() {
        return config(CONFIG_CLIENT_SECRET, "");
    }

    private String redirectUri(ClientType clientType) {
        String redirectUri = clientType == ClientType.APP
                ? config(CONFIG_APP_REDIRECT_URI, "")
                : config(CONFIG_REDIRECT_URI, "");
        if (redirectUri.isBlank()) {
            throw new OidcAuthenticationException(clientType == ClientType.APP
                    ? "OIDC app redirect URI is not configured"
                    : "OIDC redirect URI is not configured");
        }
        return redirectUri;
    }

    private String scopes() {
        String scopes = config(CONFIG_SCOPES, DEFAULT_SCOPES);
        return scopes.isBlank() ? DEFAULT_SCOPES : scopes;
    }

    private String config(String key, String defaultValue) {
        String value = systemConfigService.get(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private record OidcDiscovery(
            String issuer,
            String authorizationEndpoint,
            String tokenEndpoint,
            String jwksUri
    ) {
    }
}
