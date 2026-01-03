
package utils.createworddocuments;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;


public class CreateWordDocuments {

	public static void generateWordFromHtml(String extentHtmlPath, String screenshotBaseDir, String outputDirPath) throws Exception {
	    System.out.println("📄 Parsing: " + extentHtmlPath);
	    Document htmlDoc = Jsoup.parse(new File(extentHtmlPath), "UTF-8");

	    File outputDir = new File(outputDirPath);
	    if (!outputDir.exists()) {
	        outputDir.mkdirs();
	    }

	    Elements testItems = htmlDoc.select("li.test-item");
	    System.out.println("🧪 Found scenarios: " + testItems.size());

	    for (Element testItem : testItems) {
	        String scenarioName = testItem.selectFirst(".test-detail .name").text()
	            .replaceAll("[\\/:*?\"<>|]", "_");

	        XWPFDocument document = new XWPFDocument();

	        // Title
	        XWPFParagraph titlePara = document.createParagraph();
	        titlePara.setAlignment(ParagraphAlignment.CENTER);
	        XWPFRun titleRun = titlePara.createRun();
	        titleRun.setText(scenarioName);
	        titleRun.setBold(true);
	        titleRun.setFontSize(16);

	        // Table: Step #, Description, Status
	        XWPFTable table = document.createTable();
	        table.setWidth("100%");
	        XWPFTableRow header = table.getRow(0);
	        header.getCell(0).setText("Step #");
	        header.addNewTableCell().setText("Step Description");
	        header.addNewTableCell().setText("Status");

	        // Extract steps
	        Elements stepRows = testItem.select(".test-contents .detail-body table tbody tr.event-row");
	        System.out.println("📋 Steps found for '" + scenarioName + "': " + stepRows.size());

	        for (int i = 0; i < stepRows.size(); i++) {
	            Element row = stepRows.get(i);
	            Elements cols = row.select("td");

	            if (cols.size() >= 3) {
	                String statusText = cols.get(0).text().toUpperCase();
	                String stepText = cols.get(2).text();

	                XWPFTableRow docRow = table.createRow();
	                docRow.getCell(0).setText(String.valueOf(i + 1));
	                docRow.getCell(1).setText(stepText);
	                docRow.getCell(2).setText(statusText);
	            }
	        }

	        // Add screenshots
	        Elements imgTags = testItem.select(".test-contents img");
	        int screenshotIndex = 1;

	        for (Element imgTag : imgTags) {
	            String src = imgTag.attr("src");
	            File imageFile = new File(screenshotBaseDir, src);
	            if (imageFile.exists()) {
	                try {
	                    byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

	                    insertImageWithCaption(document,imageFile,450,250);

	                    XWPFParagraph captionPara = document.createParagraph();
	                    captionPara.setAlignment(ParagraphAlignment.CENTER);
	                    XWPFRun captionRun = captionPara.createRun();
	                    captionRun.setText(String.format("screenshot_%d", screenshotIndex++));
	                    captionRun.setBold(true);
	                    captionRun.setFontSize(10);
	                } catch (Exception e) {
	                    System.err.println("⚠️ Error adding screenshot: " + imageFile.getAbsolutePath());
	                }
	            } else {
	                System.err.println("❌ Screenshot file not found: " + imageFile.getAbsolutePath());
	            }
	        }

	        // Save file
	        String outputFileName = outputDirPath + File.separator + scenarioName + ".docx";
	        try (FileOutputStream out = new FileOutputStream(outputFileName)) {
	            document.write(out);
	            System.out.println("✅ Written: " + outputFileName);
	        } catch (IOException e) {
	            System.err.println("❌ Error writing file for: " + scenarioName);
	            e.printStackTrace();
	        }finally {
	            try {
	                if (document != null) document.close();
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }
	    }

	    System.out.println("📁 Scenario Word document generation complete.");
	    
	}
	
	private static void insertImageWithCaption(XWPFDocument document, File imageFile, int widthPx, int hightPx) throws Exception{
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        
        // Add Picture
        XWPFParagraph imagePara = document.createParagraph();
        imagePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun imageRun = imagePara.createRun();
        imageRun.addPicture(
            new ByteArrayInputStream(imageBytes),
            XWPFDocument.PICTURE_TYPE_PNG,
            imageFile.getName(),
            Units.toEMU(450),
            Units.toEMU(250)
        );
        // Add Caption
        
        XWPFParagraph captionPara = document.createParagraph();
        captionPara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun captionRun = captionPara.createRun();
        captionRun.setItalic(true);
        captionRun.setFontSize(9);
        captionRun.setText(imageFile.getName());
	}
}
