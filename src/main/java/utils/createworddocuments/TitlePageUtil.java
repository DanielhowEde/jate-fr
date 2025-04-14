package utils.createworddocuments;

import org.apache.poi.xwpf.usermodel.*;

import java.time.LocalDateTime;

public class TitlePageUtil {
    public static void insertTitlePage(XWPFDocument doc, String reportTitle) {
        XWPFParagraph titlePara = doc.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        titlePara.setVerticalAlignment(TextAlignment.CENTER);
        titlePara.setSpacingAfter(200);

        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(reportTitle);
        titleRun.setBold(true);
        titleRun.setFontSize(26);
        titleRun.addBreak();

        XWPFRun subTitleRun = titlePara.createRun();
        subTitleRun.setText("Generated from Extent HTML Report");
        subTitleRun.setFontSize(16);
        subTitleRun.addBreak();

        XWPFRun dateRun = titlePara.createRun();
        dateRun.setText("📅 " + LocalDateTime.now().toString());
        dateRun.setFontSize(12);

        XWPFParagraph breakPara = doc.createParagraph();
        breakPara.setPageBreak(true);
    }
}