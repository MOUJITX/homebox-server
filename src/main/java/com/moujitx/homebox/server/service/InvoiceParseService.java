package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.InvoiceParseResponse;
import com.moujitx.homebox.server.enums.InvoiceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceParseService {

    public InvoiceParseResponse parse(byte[] fileContent, String filename) {
        String lower = filename != null ? filename.toLowerCase() : "";
        if (lower.endsWith(".xml")) {
            return parseXml(fileContent);
        } else if (lower.endsWith(".pdf")) {
            return parsePdf(fileContent);
        } else if (lower.endsWith(".ofd")) {
            return parseOfd(fileContent);
        }
        throw new IllegalArgumentException("Unsupported file format: " + filename);
    }

    private InvoiceParseResponse parseXml(byte[] content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new ByteArrayInputStream(content)));
            doc.getDocumentElement().normalize();

            InvoiceParseResponse result = new InvoiceParseResponse();
            Element root = doc.getDocumentElement();

            result.setInvoiceCode(getElementText(root, "InvoiceCode"));
            result.setInvoiceNumber(getElementText(root, "InvoiceNumber"));

            String dateStr = getElementText(root, "InvoiceDate");
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    result.setInvoiceDate(LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE));
                } catch (Exception e) {
                    try {
                        result.setInvoiceDate(LocalDate.parse(dateStr));
                    } catch (Exception ignored) {
                    }
                }
            }

            result.setCheckCode(getElementText(root, "CheckCode"));
            result.setMachineNumber(getElementText(root, "MachineNumber"));

            // Buyer info
            Element buyer = getChildElement(root, "Buyer");
            if (buyer != null) {
                result.setBuyerName(getElementText(buyer, "BuyerName"));
                result.setBuyerTaxId(getElementText(buyer, "TaxId"));
            }

            // Seller info
            Element seller = getChildElement(root, "Seller");
            if (seller != null) {
                result.setSellerName(getElementText(seller, "SellerName"));
                result.setSellerTaxId(getElementText(seller, "TaxId"));
            }

            // Amounts
            result.setAmount(parseBigDecimal(getElementText(root, "Amount")));
            result.setTaxAmount(parseBigDecimal(getElementText(root, "TaxAmount")));
            result.setTotalAmount(parseBigDecimal(getElementText(root, "TotalAmount")));

            result.setPayee(getElementText(root, "Payee"));
            result.setReviewer(getElementText(root, "Reviewer"));
            result.setIssuer(getElementText(root, "Issuer"));
            result.setRemark(getElementText(root, "Remark"));

            result.setInvoiceType(inferInvoiceType(result));
            result.setInvoiceStatus(com.moujitx.homebox.server.enums.InvoiceStatus.NORMAL);

            return result;
        } catch (Exception e) {
            log.warn("XML parsing failed, returning empty result", e);
            return new InvoiceParseResponse();
        }
    }

    private InvoiceParseResponse parsePdf(byte[] content) {
        try {
            PDDocument document = Loader.loadPDF(content);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            InvoiceParseResponse result = new InvoiceParseResponse();
            result.setInvoiceType(InvoiceType.DIGITAL_INVOICE);
            result.setInvoiceStatus(com.moujitx.homebox.server.enums.InvoiceStatus.NORMAL);

            // Invoice number: typically 20 digits
            Pattern invoiceNumPattern = Pattern.compile("(?:发票号码|InvoiceNo)[：:]?\\s*(\\d{8,20})");
            Matcher m = invoiceNumPattern.matcher(text);
            if (m.find()) {
                result.setInvoiceNumber(m.group(1));
            }

            // Invoice code: typically 10-12 digits
            Pattern invoiceCodePattern = Pattern.compile("(?:发票代码|InvoiceCode)[：:]?\\s*(\\d{10,12})");
            m = invoiceCodePattern.matcher(text);
            if (m.find()) {
                result.setInvoiceCode(m.group(1));
            }

            // Invoice date
            Pattern datePattern = Pattern.compile("(?:开票日期|InvoiceDate)[：:]?\\s*(\\d{4})[年/-](\\d{1,2})[月/-](\\d{1,2})");
            m = datePattern.matcher(text);
            if (m.find()) {
                result.setInvoiceDate(LocalDate.of(
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3))));
            }

            // Buyer
            Pattern buyerPattern = Pattern.compile("(?:购买方|Buyer)[：:]?\\s*名[称稱]?[：:]?\\s*(.+?)(?:\\n|纳税人识别号|TaxId)");
            m = buyerPattern.matcher(text);
            if (m.find()) {
                result.setBuyerName(m.group(1).trim());
            }

            Pattern buyerTaxPattern = Pattern.compile("(?:购买方|Buyer)[\\s\\S]*?(?:纳税人识别号|统一社会信用代码|TaxId)[：:]?\\s*([A-Za-z0-9]{15,20})");
            m = buyerTaxPattern.matcher(text);
            if (m.find()) {
                result.setBuyerTaxId(m.group(1));
            }

            // Seller
            Pattern sellerPattern = Pattern.compile("(?:销售方|Seller)[：:]?\\s*名[称稱]?[：:]?\\s*(.+?)(?:\\n|纳税人识别号|TaxId)");
            m = sellerPattern.matcher(text);
            if (m.find()) {
                result.setSellerName(m.group(1).trim());
            }

            Pattern sellerTaxPattern = Pattern.compile("(?:销售方|Seller)[\\s\\S]*?(?:纳税人识别号|统一社会信用代码|TaxId)[：:]?\\s*([A-Za-z0-9]{15,20})");
            m = sellerTaxPattern.matcher(text);
            if (m.find()) {
                result.setSellerTaxId(m.group(1));
            }

            // Amounts
            Pattern amountPattern = Pattern.compile("(?:金[额額]|Amount)[：:]?\\s*[¥￥]?([\\d,.]+)");
            m = amountPattern.matcher(text);
            if (m.find()) {
                result.setAmount(parseBigDecimal(m.group(1).replace(",", "")));
            }

            Pattern taxPattern = Pattern.compile("(?:税[额額]|TaxAmount)[：:]?\\s*[¥￥]?([\\d,.]+)");
            m = taxPattern.matcher(text);
            if (m.find()) {
                result.setTaxAmount(parseBigDecimal(m.group(1).replace(",", "")));
            }

            Pattern totalPattern = Pattern.compile("(?:价税合计|TotalAmount|合[计計])[：:]?\\s*[¥￥]?([\\d,.]+)");
            m = totalPattern.matcher(text);
            if (m.find()) {
                result.setTotalAmount(parseBigDecimal(m.group(1).replace(",", "")));
            }

            // Check code: typically 20 digits
            Pattern checkCodePattern = Pattern.compile("(?:校验码|CheckCode)[：:]?\\s*(\\d{20})");
            m = checkCodePattern.matcher(text);
            if (m.find()) {
                result.setCheckCode(m.group(1));
            }

            // Payee, reviewer, issuer
            Pattern payeePattern = Pattern.compile("(?:收款人|Payee)[：:]?\\s*(\\S+)");
            m = payeePattern.matcher(text);
            if (m.find()) {
                result.setPayee(m.group(1));
            }

            Pattern reviewerPattern = Pattern.compile("(?:复核|Reviewer)[：:]?\\s*(\\S+)");
            m = reviewerPattern.matcher(text);
            if (m.find()) {
                result.setReviewer(m.group(1));
            }

            Pattern issuerPattern = Pattern.compile("(?:开票人|Issuer)[：:]?\\s*(\\S+)");
            m = issuerPattern.matcher(text);
            if (m.find()) {
                result.setIssuer(m.group(1));
            }

            return result;
        } catch (Exception e) {
            log.warn("PDF parsing failed, returning empty result", e);
            return new InvoiceParseResponse();
        }
    }

    private InvoiceParseResponse parseOfd(byte[] content) {
        try {
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(content));
            ZipEntry entry;
            String xmlContent = null;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xml") && !entry.getName().contains("META-INF")) {
                    byte[] xmlBytes = zis.readAllBytes();
                    xmlContent = new String(xmlBytes, StandardCharsets.UTF_8);
                    break;
                }
                zis.closeEntry();
            }
            zis.close();

            if (xmlContent != null) {
                return parseXml(xmlContent.getBytes(StandardCharsets.UTF_8));
            }

            return new InvoiceParseResponse();
        } catch (Exception e) {
            log.warn("OFD parsing failed, returning empty result", e);
            return new InvoiceParseResponse();
        }
    }

    private InvoiceType inferInvoiceType(InvoiceParseResponse result) {
        if (result.getInvoiceCode() != null) {
            String code = result.getInvoiceCode();
            if (code.startsWith("0")) return InvoiceType.VAT_INVOICE;
        }
        return InvoiceType.DIGITAL_INVOICE;
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            String text = nodes.item(0).getTextContent();
            return (text != null && !text.isBlank()) ? text.trim() : null;
        }
        return null;
    }

    private Element getChildElement(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0 && nodes.item(0) instanceof Element) {
            return (Element) nodes.item(0);
        }
        return null;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value.replace(",", "").replace("¥", "").replace("￥", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
