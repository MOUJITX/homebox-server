package com.moujitx.homebox.server.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.fontbox.FontBoxFont;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.font.CIDFontMapping;
import org.apache.pdfbox.pdmodel.font.FontMapper;
import org.apache.pdfbox.pdmodel.font.FontMapping;
import org.apache.pdfbox.pdmodel.font.FontMappers;
import org.apache.pdfbox.pdmodel.font.PDCIDSystemInfo;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Installs a bundled CJK font as fallback for Apache PDFBox.
 *
 * <p>PDFBox renders fonts that are not embedded in a PDF with fonts found on the operating
 * system. Many Chinese PDF invoices only reference fonts by name (KaiTi / SimSun / SimHei, ...)
 * without embedding them, so on a host without CJK fonts - the Alpine based Docker image has
 * none - PDFBox substitutes its Latin-only last-resort font and silently drops every CJK glyph
 * from the rendered preview.
 *
 * <p>Instead of relying on host fonts, the server ships a small CJK font and maps non-embedded
 * CID fonts that use a CJK character collection to it whenever PDFBox would otherwise fall back
 * to its last-resort font. Hosts that do have the referenced fonts are left untouched.
 */
@Configuration
@Slf4j
public class PdfFontConfig {

    private static final String FALLBACK_FONT_RESOURCE = "/fonts/wqy-microhei.ttc";
    private static final String FALLBACK_FONT_NAME = "WenQuanYiMicroHei";

    private static final Set<String> CJK_CHARACTER_COLLECTIONS = Set.of(
            "Adobe-GB1", "Adobe-CNS1", "Adobe-Japan1", "Adobe-Korea1");

    private static boolean installed;

    @PostConstruct
    synchronized void installCjkFallbackFont() {
        if (installed) {
            return;
        }
        try {
            TrueTypeFont cjkFont = loadFallbackFont();
            FontMappers.set(new CjkFallbackFontMapper(FontMappers.instance(), cjkFont));
            installed = true;
            log.info("Installed CJK fallback font '{}' for PDFBox", cjkFont.getName());
        } catch (IOException e) {
            // Not fatal: PDFBox keeps its default substitution, only CJK text of non-embedded
            // fonts may be missing when the host has no CJK fonts.
            log.warn("Could not install the bundled CJK fallback font, PDF previews may miss CJK text", e);
        }
    }

    private static TrueTypeFont loadFallbackFont() throws IOException {
        try (InputStream in = PdfFontConfig.class.getResourceAsStream(FALLBACK_FONT_RESOURCE)) {
            if (in == null) {
                throw new IOException("Font resource not found: " + FALLBACK_FONT_RESOURCE);
            }
            TrueTypeCollection collection = new TrueTypeCollection(in);
            TrueTypeFont font = collection.getFontByName(FALLBACK_FONT_NAME);
            if (font == null) {
                throw new IOException(
                        "Font " + FALLBACK_FONT_NAME + " not found in " + FALLBACK_FONT_RESOURCE);
            }
            return font;
        }
    }

    /**
     * Wraps PDFBox's default {@link FontMapper}: if a non-embedded CID font belongs to a CJK
     * character collection and PDFBox could only resolve it with its last-resort fallback, the
     * bundled CJK font is used instead.
     */
    private static final class CjkFallbackFontMapper implements FontMapper {

        private final FontMapper delegate;
        private final TrueTypeFont cjkFont;

        private CjkFallbackFontMapper(FontMapper delegate, TrueTypeFont cjkFont) {
            this.delegate = delegate;
            this.cjkFont = cjkFont;
        }

        @Override
        public FontMapping<TrueTypeFont> getTrueTypeFont(String baseFont, PDFontDescriptor fontDescriptor) {
            return delegate.getTrueTypeFont(baseFont, fontDescriptor);
        }

        @Override
        public FontMapping<FontBoxFont> getFontBoxFont(String baseFont, PDFontDescriptor fontDescriptor) {
            return delegate.getFontBoxFont(baseFont, fontDescriptor);
        }

        @Override
        public CIDFontMapping getCIDFont(String baseFont, PDFontDescriptor fontDescriptor,
                PDCIDSystemInfo cidSystemInfo) {
            CIDFontMapping mapping = delegate.getCIDFont(baseFont, fontDescriptor, cidSystemInfo);
            if (mapping.isFallback() && isCjkCharacterCollection(cidSystemInfo)) {
                log.debug("Using bundled CJK font for non-embedded CID font '{}'", baseFont);
                return new CIDFontMapping(null, cjkFont, true);
            }
            return mapping;
        }

        private static boolean isCjkCharacterCollection(PDCIDSystemInfo cidSystemInfo) {
            if (cidSystemInfo == null) {
                return false;
            }
            String registry = cidSystemInfo.getRegistry();
            String ordering = cidSystemInfo.getOrdering();
            return registry != null && ordering != null
                    && CJK_CHARACTER_COLLECTIONS.contains(registry + "-" + ordering);
        }
    }
}
