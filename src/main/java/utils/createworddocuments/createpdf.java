package utils.createworddocuments;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

public class createpdf {

    /**
     * Builds a PDF per scenario in an Extent HTML report.
     * @param extentHtmlPath path to Extent HTML report
     * @param screenshotBaseDir base dir where report images live (src attr is resolved relative to this)
     * @param outputDirPath where PDFs will be written
     */
    public static void generatePdfFromHtml(String extentHtmlPath, String screenshotBaseDir, String outputDirPath) throws Exception {
        System.out.println("📄 Parsing: " + extentHtmlPath);
        Document htmlDoc = Jsoup.parse(new File(extentHtmlPath), "UTF-8");

        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) outputDir.mkdirs();

        Elements testItems = htmlDoc.select("li.test-item");
        System.out.println("🧪 Found scenarios: " + testItems.size());

        // Fonts (register system fonts; fall back to built-ins)
        Font titleFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font cellFont    = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font captionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

        for (Element testItem : testItems) {
            String scenarioName = testItem.selectFirst(".test-detail .name")
                    .text().replaceAll("[\\/:*?\"<>|]", "_");

            String outputFileName = outputDirPath + File.separator + scenarioName + ".pdf";
            try (FileOutputStream fos = new FileOutputStream(outputFileName)) {

                // Page setup
                com.lowagie.text.Document pdf = new com.lowagie.text.Document(PageSize.A4, 36, 36, 36, 36);
                PdfWriter.getInstance(pdf, fos);
                pdf.addAuthor("CreatePdfDocuments");
                pdf.addTitle(scenarioName);
                pdf.open();

                // Title
                Paragraph title = new Paragraph(scenarioName, titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(12f);
                pdf.add(title);

                // Table (Step #, Description, Status)
                float[] widths = { 1.0f, 6.0f, 1.5f };
                PdfPTable table = new PdfPTable(widths);
                table.setWidthPercentage(100);
                table.setSpacingBefore(6f);
                table.setSpacingAfter(10f);

                // Header row
                table.addCell(makeHeaderCell("Step #", headerFont));
                table.addCell(makeHeaderCell("Step Description", headerFont));
                table.addCell(makeHeaderCell("Status", headerFont));

                // Extract steps from Extent
                Elements stepRows = testItem.select(".test-contents .detail-body table tbody tr.event-row");
                System.out.println("📋 Steps for '" + scenarioName + "': " + stepRows.size());

                for (int i = 0; i < stepRows.size(); i++) {
                    Element row = stepRows.get(i);
                    Elements cols = row.select("td");
                    if (cols.size() >= 3) {
                        String statusText = cols.get(0).text().toUpperCase();
                        String stepText   = cols.get(2).text();

                        table.addCell(makeBodyCell(String.valueOf(i + 1), cellFont));
                        table.addCell(makeBodyCell(stepText, cellFont));
                        table.addCell(makeBodyCell(statusText, cellFont));
                    }
                }

                pdf.add(table);

                // Screenshots (centered, scale to fit, caption)
                Elements imgTags = testItem.select(".test-contents img");
                int screenshotIndex = 1;
                for (Element imgTag : imgTags) {
                    String src = imgTag.attr("src");
                    File imageFile = new File(screenshotBaseDir, src);

                    if (!imageFile.exists()) {
                        System.err.println("❌ Screenshot file not found: " + imageFile.getAbsolutePath());
                        continue;
                    }

                    try {
                        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                        Image img = Image.getInstance(imageBytes);

                        // scale and center
                        img.scaleToFit(450f, 250f);
                        img.setAlignment(Image.MIDDLE);
                        img.setSpacingBefore(6f);
                        img.setSpacingAfter(2f);
                        pdf.add(img);

                        Paragraph caption = new Paragraph(
                                String.format("screenshot_%d (%s)", screenshotIndex++, imageFile.getName()),
                                captionFont
                        );
                        caption.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                        caption.setSpacingAfter(8f);
                        pdf.add(caption);

                    } catch (Exception e) {
                        System.err.println("⚠️ Error adding screenshot: " + imageFile.getAbsolutePath() + " : " + e.getMessage());
                    }
                }

                pdf.close();
                System.out.println("✅ Written: " + outputFileName);
            }
        }

        System.out.println("📁 Scenario PDF generation complete.");
    }

    // === Helpers ===

    private static PdfPCell makeHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        cell.setBackgroundColor(new Color(230, 230, 230));
        return cell;
    }

    private static PdfPCell makeBodyCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f);
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        return cell;
    }
}