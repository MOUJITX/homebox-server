package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.response.InvoiceParseResponse;
import com.moujitx.homebox.server.enums.InvoiceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ofdrw.converter.ImageMaker;
import org.ofdrw.reader.ContentExtractor;
import org.ofdrw.reader.OFDReader;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceParseService {

    private final AiService aiService;

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

            // TaxSupervisionInfo: invoice number and date
            Element taxInfo = getChildElement(root, "TaxSupervisionInfo");
            if (taxInfo != null) {
                result.setInvoiceNumber(getElementText(taxInfo, "InvoiceNumber"));
                String dateStr = getElementText(taxInfo, "IssueTime");
                if (dateStr != null && !dateStr.isEmpty()) {
                    try {
                        result.setInvoiceDate(LocalDate.parse(dateStr.substring(0, 10)));
                    } catch (Exception ignored) {
                    }
                }
            }

            // EInvoiceData: seller, buyer, amounts, remark
            Element data = getChildElement(root, "EInvoiceData");
            if (data != null) {
                // Seller info
                Element seller = getChildElement(data, "SellerInformation");
                if (seller != null) {
                    result.setSellerName(getElementText(seller, "SellerName"));
                    result.setSellerTaxId(getElementText(seller, "SellerIdNum"));
                }

                // Buyer info
                Element buyer = getChildElement(data, "BuyerInformation");
                if (buyer != null) {
                    result.setBuyerName(getElementText(buyer, "BuyerName"));
                    result.setBuyerTaxId(getElementText(buyer, "BuyerIdNum"));
                }

                // Amounts
                Element basic = getChildElement(data, "BasicInformation");
                if (basic != null) {
                    result.setAmount(parseBigDecimal(getElementText(basic, "TotalAmWithoutTax")));
                    result.setTaxAmount(parseBigDecimal(getElementText(basic, "TotalTaxAm")));
                    result.setTotalAmount(parseBigDecimal(getElementText(basic, "TotalTax-includedAmount")));
                }

                // Remark
                Element additional = getChildElement(data, "AdditionalInformation");
                if (additional != null) {
                    result.setRemark(getElementText(additional, "Remark"));
                }
            }

            // Invoice type from Header/InherentLabel/EInvoiceType
            Element header = getChildElement(root, "Header");
            if (header != null) {
                Element inherent = getChildElement(header, "InherentLabel");
                if (inherent != null) {
                    Element eInvoiceType = getChildElement(inherent, "EInvoiceType");
                    Element generalOrSpecialVAT = getChildElement(inherent, "GeneralOrSpecialVAT");
                    if (eInvoiceType != null) {
                        result.setInvoiceType(
                                mapInvoiceType(
                                        getElementText(eInvoiceType, "LabelCode"),
                                        getElementText(generalOrSpecialVAT, "LabelCode")));
                    }
                    Element issuType = getChildElement(inherent, "InIssuType");
                    if (issuType != null) {
                        result.setInvoiceStatus("Y".equals(getElementText(issuType, "LabelCode"))
                                ? com.moujitx.homebox.server.enums.InvoiceStatus.NORMAL
                                : com.moujitx.homebox.server.enums.InvoiceStatus.RED_FLUSHED);
                    }
                }
            }

            if (result.getInvoiceType() == null) {
                result.setInvoiceType(InvoiceType.DIGITAL_INVOICE);
            }
            if (result.getInvoiceStatus() == null) {
                result.setInvoiceStatus(com.moujitx.homebox.server.enums.InvoiceStatus.NORMAL);
            }

            return result;
        } catch (Exception e) {
            log.warn("XML parsing failed, returning empty result", e);
            return new InvoiceParseResponse();
        }
    }

    private InvoiceParseResponse parsePdf(byte[] content) {
        try {
            PDDocument document = PDDocument.load(content);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            String previewImage = renderPdfPreview(document);
            document.close();

            log.debug("Extracted PDF text:\n{}", text);
            InvoiceParseResponse result = aiService.extractInvoiceInfo(text);
            result.setPreviewImage(previewImage);
            return result;
        } catch (Exception e) {
            log.warn("PDF parsing failed, returning empty result", e);
            return new InvoiceParseResponse();
        }
    }

    private InvoiceParseResponse parseOfd(byte[] content) {
        try (OFDReader reader = new OFDReader(new ByteArrayInputStream(content))) {
            ContentExtractor extractor = new ContentExtractor(reader);
            List<String> textList = extractor.extractAll();
            String text = String.join("\n", textList);
            log.debug("Extracted OFD text content:\n{}", text);

            String previewImage = renderOfdPreview(reader);

            InvoiceParseResponse result = aiService.extractInvoiceInfo(text);
            result.setPreviewImage(previewImage);
            return result;
        } catch (Exception e) {
            log.warn("OFD parsing failed, returning empty result", e);
            return new InvoiceParseResponse();
        }
    }

    private String renderPdfPreview(PDDocument document) {
        try {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 150, ImageType.RGB);
            String base64 = imageToBase64(image);
            image.flush();
            return base64;
        } catch (Exception e) {
            log.warn("Failed to render PDF preview image", e);
            return null;
        }
    }

    public String renderPdfPreview(byte[] content) {
        try (PDDocument document = PDDocument.load(content)) {
            return renderPdfPreview(document);
        } catch (Exception e) {
            log.warn("Failed to render PDF preview from bytes", e);
            return null;
        }
    }

    private String renderOfdPreview(OFDReader reader) {
        try {
            ImageMaker maker = new ImageMaker(reader, 6);
            BufferedImage image = maker.makePage(0);
            String base64 = imageToBase64(image);
            image.flush();
            return base64;
        } catch (Exception e) {
            log.warn("Failed to render OFD preview image", e);
            return null;
        }
    }

    public String renderOfdPreview(byte[] content) {
        try (OFDReader reader = new OFDReader(new ByteArrayInputStream(content))) {
            return renderOfdPreview(reader);
        } catch (Exception e) {
            log.warn("Failed to render OFD preview from bytes", e);
            return null;
        }
    }

    private String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            log.warn("Failed to convert image to base64", e);
            return null;
        }
    }

    private InvoiceType mapInvoiceType(String eInvoiceType, String generalOrSpecialVAT) {
        if (eInvoiceType.equals("01")) {
            return switch (generalOrSpecialVAT) {
                case "02" -> InvoiceType.DIGITAL_INVOICE;
                default -> InvoiceType.OTHER;
            };
        }

        return InvoiceType.OTHER;
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
        if (value == null || value.isBlank())
            return null;
        try {
            return new BigDecimal(value.replace(",", "").replace("¥", "").replace("￥", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
