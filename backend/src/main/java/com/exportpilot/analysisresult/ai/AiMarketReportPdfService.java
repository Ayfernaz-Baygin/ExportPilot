package com.exportpilot.analysisresult.ai;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiMarketReportPdfService {

    private static final float MARGIN = 55;
    private static final float TITLE_FONT_SIZE = 18;
    private static final float HEADING_FONT_SIZE = 13;
    private static final float BODY_FONT_SIZE = 10.5f;
    private static final float LINE_HEIGHT = 15;
    private static final float PARAGRAPH_SPACE = 7;

    private final AiMarketReportService reportService;

    public AiMarketReportPdfService(
            AiMarketReportService reportService
    ) {
        this.reportService = reportService;
    }

    public byte[] generatePdf(Long analysisId) {
        AiMarketReport report =
                reportService.generateReport(analysisId);

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream();
                InputStream regularFontStream =
                        new ClassPathResource(
                                "fonts/NotoSans-Regular.ttf"
                        ).getInputStream();
                InputStream boldFontStream =
                        new ClassPathResource(
                                "fonts/NotoSans-Bold.ttf"
                        ).getInputStream()
        ) {
            PDFont regularFont = PDType0Font.load(
                    document,
                    regularFontStream
            );

            PDFont boldFont = PDType0Font.load(
                    document,
                    boldFontStream
            );

            addDocumentMetadata(
                    document,
                    report
            );

            PdfPageWriter writer = new PdfPageWriter(
                    document,
                    regularFont,
                    boldFont
            );

            writer.writeTitle("ExportPilot Pazar Değerlendirme Raporu");

            writer.writeMetadataLine(
                    "Analiz ID",
                    String.valueOf(report.analysisId())
            );

            writer.writeMetadataLine(
                    "Yapay zekâ modeli",
                    report.model()
            );

            writer.addVerticalSpace(14);

            writeReportContent(
                    writer,
                    report.report()
            );

            writer.close();

            document.save(outputStream);

            return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "PDF raporu olusturulamadi.",
                    exception
            );
        }
    }

    private void addDocumentMetadata(
            PDDocument document,
            AiMarketReport report
    ) {
        PDDocumentInformation information =
                new PDDocumentInformation();

        information.setTitle(
                "ExportPilot Analiz "
                        + report.analysisId()
                        + " Pazar Raporu"
        );

        information.setAuthor("ExportPilot");
        information.setSubject(
                "AI destekli ihracat pazarı değerlendirme raporu"
        );

        information.setKeywords(
                "ExportPilot, ihracat, pazar analizi, yapay zeka"
        );

        document.setDocumentInformation(information);
    }

    private void writeReportContent(
            PdfPageWriter writer,
            String reportText
    ) throws IOException {
        if (reportText == null || reportText.isBlank()) {
            writer.writeBodyText(
                    "Bu analiz için rapor içeriği bulunamadı."
            );
            return;
        }

        String normalizedReport = reportText
                .replace("\r\n", "\n")
                .replace("\r", "\n");

        String[] lines = normalizedReport.split("\n");

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isBlank()) {
                writer.addVerticalSpace(PARAGRAPH_SPACE);
                continue;
            }

            if (isMainTitle(line)) {
                writer.writeHeading(
                        removeMarkdown(line),
                        14
                );
                continue;
            }

            if (isSectionHeading(line)) {
                writer.addVerticalSpace(4);
                writer.writeHeading(
                        removeMarkdown(line),
                        HEADING_FONT_SIZE
                );
                continue;
            }

            if (isBulletPoint(line)) {
                writer.writeBullet(
                        removeBulletMarkdown(line)
                );
                continue;
            }

            writer.writeBodyText(
                    removeMarkdown(line)
            );

            writer.addVerticalSpace(PARAGRAPH_SPACE);
        }
    }

    private boolean isMainTitle(String line) {
        String cleaned = removeMarkdown(line).toUpperCase();

        return cleaned.startsWith("PAZAR DEĞERLENDİRME RAPORU");
    }

    private boolean isSectionHeading(String line) {
        String cleaned = removeMarkdown(line);

        return cleaned.matches(
                "^\\d+\\..+"
        );
    }

    private boolean isBulletPoint(String line) {
        return line.startsWith("* ")
                || line.startsWith("- ")
                || line.startsWith("• ")
                || line.matches("^\\*\\s{2,}.+");
    }

    private String removeBulletMarkdown(String line) {
        String cleaned = line
                .replaceFirst("^\\*\\s+", "")
                .replaceFirst("^-\\s+", "")
                .replaceFirst("^•\\s+", "");

        return removeMarkdown(cleaned);
    }

    private String removeMarkdown(String text) {
        return text
                .replace("**", "")
                .replace("__", "")
                .replace("`", "")
                .trim();
    }

    private static class PdfPageWriter {

        private final PDDocument document;
        private final PDFont regularFont;
        private final PDFont boldFont;

        private PDPage page;
        private PDPageContentStream contentStream;
        private float currentY;

        private PdfPageWriter(
                PDDocument document,
                PDFont regularFont,
                PDFont boldFont
        ) throws IOException {
            this.document = document;
            this.regularFont = regularFont;
            this.boldFont = boldFont;

            createNewPage();
        }

        private void createNewPage() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }

            page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            contentStream = new PDPageContentStream(
                    document,
                    page
            );

            currentY = page.getMediaBox().getHeight() - MARGIN;
        }

        private void writeTitle(String title) throws IOException {
            ensureSpace(35);

            writeWrappedText(
                    title,
                    boldFont,
                    TITLE_FONT_SIZE,
                    22,
                    0
            );

            addVerticalSpace(8);
            drawHorizontalLine();
            addVerticalSpace(14);
        }

        private void writeMetadataLine(
                String label,
                String value
        ) throws IOException {
            ensureSpace(20);

            String text = label + ": " + value;

            writeWrappedText(
                    text,
                    regularFont,
                    BODY_FONT_SIZE,
                    LINE_HEIGHT,
                    0
            );
        }

        private void writeHeading(
                String heading,
                float fontSize
        ) throws IOException {
            ensureSpace(30);

            writeWrappedText(
                    heading,
                    boldFont,
                    fontSize,
                    18,
                    0
            );

            addVerticalSpace(5);
        }

        private void writeBodyText(
                String text
        ) throws IOException {
            writeWrappedText(
                    text,
                    regularFont,
                    BODY_FONT_SIZE,
                    LINE_HEIGHT,
                    0
            );
        }

        private void writeBullet(
                String text
        ) throws IOException {
            writeWrappedText(
                    "• " + text,
                    regularFont,
                    BODY_FONT_SIZE,
                    LINE_HEIGHT,
                    15
            );

            addVerticalSpace(3);
        }

        private void writeWrappedText(
                String text,
                PDFont font,
                float fontSize,
                float lineHeight,
                float leftIndent
        ) throws IOException {
            float availableWidth =
                    page.getMediaBox().getWidth()
                            - (2 * MARGIN)
                            - leftIndent;

            List<String> wrappedLines = wrapText(
                    text,
                    font,
                    fontSize,
                    availableWidth
            );

            for (String line : wrappedLines) {
                ensureSpace(lineHeight);

                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(
                        MARGIN + leftIndent,
                        currentY
                );
                contentStream.showText(line);
                contentStream.endText();

                currentY -= lineHeight;
            }
        }

        private List<String> wrapText(
                String text,
                PDFont font,
                float fontSize,
                float maximumWidth
        ) throws IOException {
            List<String> lines = new ArrayList<>();

            if (text == null || text.isBlank()) {
                lines.add("");
                return lines;
            }

            String[] words = text.trim().split("\\s+");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                String candidate = currentLine.isEmpty()
                        ? word
                        : currentLine + " " + word;

                float candidateWidth =
                        font.getStringWidth(candidate)
                                / 1000
                                * fontSize;

                if (candidateWidth <= maximumWidth) {
                    currentLine.setLength(0);
                    currentLine.append(candidate);
                    continue;
                }

                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                }

                if (
                        font.getStringWidth(word)
                                / 1000
                                * fontSize
                                <= maximumWidth
                ) {
                    currentLine.append(word);
                } else {
                    lines.addAll(
                            splitLongWord(
                                    word,
                                    font,
                                    fontSize,
                                    maximumWidth
                            )
                    );
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }

            return lines;
        }

        private List<String> splitLongWord(
                String word,
                PDFont font,
                float fontSize,
                float maximumWidth
        ) throws IOException {
            List<String> parts = new ArrayList<>();
            StringBuilder currentPart = new StringBuilder();

            for (char character : word.toCharArray()) {
                String candidate =
                        currentPart.toString() + character;

                float candidateWidth =
                        font.getStringWidth(candidate)
                                / 1000
                                * fontSize;

                if (
                        candidateWidth > maximumWidth
                                && !currentPart.isEmpty()
                ) {
                    parts.add(currentPart.toString());
                    currentPart.setLength(0);
                }

                currentPart.append(character);
            }

            if (!currentPart.isEmpty()) {
                parts.add(currentPart.toString());
            }

            return parts;
        }

        private void ensureSpace(
                float requiredHeight
        ) throws IOException {
            if (
                    currentY - requiredHeight
                            < MARGIN
            ) {
                createNewPage();
            }
        }

        private void addVerticalSpace(
                float space
        ) throws IOException {
            ensureSpace(space);
            currentY -= space;
        }

        private void drawHorizontalLine()
                throws IOException {
            ensureSpace(5);

            contentStream.moveTo(
                    MARGIN,
                    currentY
            );

            contentStream.lineTo(
                    page.getMediaBox().getWidth() - MARGIN,
                    currentY
            );

            contentStream.setLineWidth(0.7f);
            contentStream.stroke();
        }

        private void close() throws IOException {
            if (contentStream != null) {
                contentStream.close();
                contentStream = null;
            }
        }
    }
}