package utils.createworddocuments;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFldCharType;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.Base64;

public class CreateWordDocumentsold {
    public static void generateWordFromHtml(String extentHtmlPath, String screenshotBaseDir, String outputDocx, String reportTitle) throws Exception {
        System.out.println("\uD83D\uDCC4 Parsing: " + extentHtmlPath);
        Document htmlDoc = Jsoup.parse(new File(extentHtmlPath), "UTF-8");

        XWPFDocument wordDoc = new XWPFDocument();
        FileOutputStream out = new FileOutputStream(outputDocx);

        // Insert title page
        TitlePageUtil.insertTitlePage(wordDoc, reportTitle);

        // Table of Contents title
        XWPFParagraph tocTitle = wordDoc.createParagraph();
        tocTitle.setStyle("Heading1");
        setOutlineLevel(tocTitle, 0);
        XWPFRun tocRun = tocTitle.createRun();
        tocRun.setText("\uD83D\uDCD1 Table of Contents");
        tocRun.setBold(true);
        tocRun.setFontSize(16);
        tocRun.setFontFamily("Calibri");
        tocRun.setColor("2E74B5");

        // Add TOC field
        XWPFParagraph fieldPara = wordDoc.insertNewParagraph(tocTitle.getCTP().newCursor());
        XWPFRun fieldRun = fieldPara.createRun();
        fieldRun.getCTR().addNewFldChar().setFldCharType(STFldCharType.BEGIN);
        fieldRun = fieldPara.createRun();
        fieldRun.setText(" TOC \\o \"1-3\" \\h \\z \\u ");
        fieldRun = fieldPara.createRun();
        fieldRun.getCTR().addNewFldChar().setFldCharType(STFldCharType.SEPARATE);
        fieldRun = fieldPara.createRun();
        fieldRun.setText("Right-click to update field.");
        fieldRun = fieldPara.createRun();
        fieldRun.getCTR().addNewFldChar().setFldCharType(STFldCharType.END);

        Elements testItems = htmlDoc.select("li.test-item");

        for (Element testItem : testItems) {
            wordDoc.createParagraph().setPageBreak(true);

            // Test name header
            String testName = testItem.selectFirst(".test-detail .name").text();
            XWPFParagraph titlePara = wordDoc.createParagraph();
            titlePara.setStyle("Heading2");
            setOutlineLevel(titlePara, 1);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText("\uD83E\uDDEA " + testName);
            titleRun.setBold(true);
            titleRun.setFontSize(14);
            titleRun.setFontFamily("Calibri");
            titleRun.setColor("1F4E79");

            Elements rows = testItem.select(".detail-body table tbody tr");
            Map<String, List<Element>> statusMap = new LinkedHashMap<>();
            for (Element row : rows) {
                String status = row.select("td").get(0).text();
                statusMap.computeIfAbsent(status, k -> new ArrayList<>()).add(row);
            }

            for (Map.Entry<String, List<Element>> entry : statusMap.entrySet()) {
                String status = entry.getKey();
                List<Element> stepRows = entry.getValue();

                XWPFParagraph statusPara = wordDoc.createParagraph();
                statusPara.setStyle("Heading3");
                setOutlineLevel(statusPara, 2);
                XWPFRun statusRun = statusPara.createRun();
                statusRun.setText("\uD83D\uDCCC " + status + " Steps");
                statusRun.setBold(true);
                statusRun.setFontSize(12);
                statusRun.setFontFamily("Calibri");
                statusRun.setColor("5B9BD5");

                XWPFTable table = wordDoc.createTable(stepRows.size() + 1, 3);
                table.setWidth("100%");
                XWPFTableRow headerRow = table.getRow(0);
                String[] headers = {"Status", "Timestamp", "Details"};

                for (int i = 0; i < 3; i++) {
                    headerRow.getCell(i).setText(headers[i]);
                    XWPFParagraph para = headerRow.getCell(i).getParagraphs().get(0);
                    XWPFRun run = para.createRun();
                    run.setBold(true);
                    run.setFontFamily("Calibri");
                    run.setColor("FFFFFF");
                    headerRow.getCell(i).setColor("4472C4");
                }

                for (int i = 0; i < stepRows.size(); i++) {
                    Element row = stepRows.get(i);
                    Elements cols = row.select("td");
                    if (cols.size() == 3) {
                        for (int j = 0; j < 3; j++) {
                            table.getRow(i + 1).getCell(j).setText(cols.get(j).text());
                        }
                    }
                }
            }

            Elements imageElements = testItem.select("img");
            int imgCount = 1;
            for (Element img : imageElements) {
                String imgSrc = img.attr("src");
                XWPFParagraph imgPara = wordDoc.createParagraph();
                XWPFRun imgRun = imgPara.createRun();

                try (InputStream in = imgSrc.startsWith("data:image") ?
                        new ByteArrayInputStream(Base64.getDecoder().decode(imgSrc.split(",")[1])) :
                        new FileInputStream(new File(screenshotBaseDir, new File(imgSrc).getName()))) {

                    imgRun.addPicture(in, XWPFDocument.PICTURE_TYPE_PNG, "Screenshot_" + imgCount + ".png",
                            Units.toEMU(500), Units.toEMU(300));

                    XWPFParagraph caption = wordDoc.createParagraph();
                    caption.setAlignment(ParagraphAlignment.CENTER);
                    XWPFRun captionRun = caption.createRun();
                    captionRun.setText("\uD83D\uDCF8 Screenshot " + imgCount);
                    captionRun.setItalic(true);
                    captionRun.setFontFamily("Calibri");
                    captionRun.setFontSize(10);
                    imgCount++;

                } catch (Exception e) {
                    System.out.println("❌ Error inserting image: " + imgSrc + " → " + e.getMessage());
                }
            }
        }

        wordDoc.write(out);
        out.close();
        wordDoc.close();
        System.out.println("✅ Word document created: " + outputDocx);
    }

    private static void setOutlineLevel(XWPFParagraph para, int level) {
        CTP ctp = para.getCTP();
        CTPPr pPr = ctp.isSetPPr() ? ctp.getPPr() : ctp.addNewPPr();
        CTDecimalNumber outlineLvl = pPr.isSetOutlineLvl() ? pPr.getOutlineLvl() : pPr.addNewOutlineLvl();
        outlineLvl.setVal(BigInteger.valueOf(level));
    }
}
