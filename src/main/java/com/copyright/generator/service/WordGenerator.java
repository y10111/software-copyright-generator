package com.copyright.generator.service;

import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * Word文档生成器
 * <p>
 * 使用Apache POI生成符合软件著作权申请要求的Word(.docx)文档。
 * 文档规范：
 * - 每页50行代码
 * - 中文宋体(SimSun)，英文/数字Times New Roman，五号(10.5pt)
 * - 页眉：左(软件名称+版本号)、中(程序鉴别材料)、右(页码)，加粗
 * - 页脚：右(第X页 of 总页数)，加粗
 * - A4纸张，上下边距1080 twips，左右边距720 twips
 * </p>
 *
 * @author vx公众号：全粘程序员
 * @version 1.0.0
 * @since 2026-08-16
 */
public class WordGenerator {

    /** 中文字体（Windows使用SimSun，macOS自动回退到系统宋体） */
    private static final String FONT_CN = "SimSun";

    /** 英文字体 */
    private static final String FONT_EN = "Times New Roman";

    /** 字体大小（五号 = 10.5pt = 21 half-points） */
    private static final int FONT_SIZE = 21;

    /** 页眉页脚字体大小（小五号 = 9pt = 18 half-points） */
    private static final int HEADER_FONT_SIZE = 18;

    /** 行距 (twips)，293 twips = 14.65pt，A4正文区14679/50≈293.58，用293让底部留白仅29twips */
    private static final int LINE_SPACING = 293;

    /** 页边距 - 上下 (twips) */
    private static final int MARGIN_TOP_BOTTOM = 1080;

    /** 页边距 - 左右 (twips) */
    private static final int MARGIN_LEFT_RIGHT = 720;

    /** 页眉距顶端距离 (twips) */
    private static final int HEADER_MARGIN = 540;

    /** 页脚距底端距离 (twips) */
    private static final int FOOTER_MARGIN = 540;

    /** 软件名称 */
    private final String softwareName;

    /** 版本号 */
    private final String version;

    /**
     * 构造Word文档生成器
     *
     * @param softwareName 软件名称
     * @param version      版本号
     */
    public WordGenerator(String softwareName, String version) {
        this.softwareName = softwareName;
        this.version = version;
    }

    /**
     * 生成Word文档
     *
     * @param outputFile 输出文件路径
     * @param lines      代码行列表
     * @throws IOException 写入文件失败时抛出
     */
    public void generate(File outputFile, List<String> lines) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            // 移除文档可能存在的默认空段落
            int bodyElementCount = document.getBodyElements().size();
            for (int i = bodyElementCount - 1; i >= 0; i--) {
                if (document.getBodyElements().get(i) instanceof XWPFParagraph) {
                    document.removeBodyElement(i);
                }
            }

            // 设置页面
            setupPage(document);
            // 设置页眉
            setupHeader(document);
            // 设置页脚
            setupFooter(document);
            // 写入代码内容
            writeContent(document, lines);
            // 保存文件
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                document.write(fos);
            }
        }
    }

    /**
     * 设置页面参数
     */
    private void setupPage(XWPFDocument document) {
        CTDocument1 ctDocument = document.getDocument();
        CTBody ctBody = ctDocument.getBody();
        if (ctBody == null) {
            return;
        }

        CTSectPr sectPr = ctBody.isSetSectPr() ? ctBody.getSectPr() : ctBody.addNewSectPr();
        CTPageMar pageMar = sectPr.isSetPgMar() ? sectPr.getPgMar() : sectPr.addNewPgMar();

        pageMar.setTop(BigInteger.valueOf(MARGIN_TOP_BOTTOM));
        pageMar.setBottom(BigInteger.valueOf(MARGIN_TOP_BOTTOM));
        pageMar.setLeft(BigInteger.valueOf(MARGIN_LEFT_RIGHT));
        pageMar.setRight(BigInteger.valueOf(MARGIN_LEFT_RIGHT));
        // 约束页眉页脚位置，防止侵占正文区域
        pageMar.setHeader(BigInteger.valueOf(HEADER_MARGIN));
        pageMar.setFooter(BigInteger.valueOf(FOOTER_MARGIN));

        // A4纸张大小
        CTPageSz pageSz = sectPr.isSetPgSz() ? sectPr.getPgSz() : sectPr.addNewPgSz();
        pageSz.setW(BigInteger.valueOf(11906)); // A4宽度
        pageSz.setH(BigInteger.valueOf(16838)); // A4高度
    }

    /**
     * 设置页眉
     * <p>
     * 页眉分为三部分：左(软件名称+版本号)、中(程序鉴别材料)、右(页码)，均加粗
     * </p>
     */
    private void setupHeader(XWPFDocument document) {
        XWPFHeaderFooterPolicy policy = document.getHeaderFooterPolicy();
        if (policy == null) {
            policy = document.createHeaderFooterPolicy();
        }

        XWPFHeader header = policy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);
        List<XWPFParagraph> paragraphs = header.getParagraphs();
        XWPFParagraph paragraph = !paragraphs.isEmpty()
                ? paragraphs.get(0) : header.createParagraph();

        // 紧凑间距
        CTPPr hPPr = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();
        CTSpacing hSpacing = hPPr.isSetSpacing() ? hPPr.getSpacing() : hPPr.addNewSpacing();
        hSpacing.setBefore(BigInteger.ZERO);
        hSpacing.setAfter(BigInteger.ZERO);

        // 设置段落对齐和制表位
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        CTTabStop tabStopCenter = hPPr.addNewTabs().addNewTab();
        tabStopCenter.setVal(STTabJc.CENTER);
        tabStopCenter.setPos(BigInteger.valueOf(4500));

        CTTabStop tabStopRight = hPPr.addNewTabs().addNewTab();
        tabStopRight.setVal(STTabJc.RIGHT);
        tabStopRight.setPos(BigInteger.valueOf(9000));

        // 左部分：软件名称+版本号
        XWPFRun leftRun = paragraph.createRun();
        setRunFont(leftRun, HEADER_FONT_SIZE, true);
        leftRun.setText(softwareName + " " + version);

        // 中间部分：程序鉴别材料
        XWPFRun centerRun = paragraph.createRun();
        setRunFont(centerRun, HEADER_FONT_SIZE, true);
        centerRun.setText("\t程序鉴别材料");

        // 右部分：页码
        XWPFRun rightRun = paragraph.createRun();
        setRunFont(rightRun, HEADER_FONT_SIZE, true);
        rightRun.setText("\t");

        // 添加页码域
        CTP ctp = paragraph.getCTP();
        CTSimpleField pageField = ctp.addNewFldSimple();
        pageField.setInstr("PAGE");
        CTR runForPage = pageField.addNewR();
        CTText pageText = runForPage.addNewT();
        pageText.setStringValue("1");
    }

    /**
     * 设置页脚
     * <p>
     * 页脚右对齐显示：第X页 of 总页数，加粗
     * </p>
     */
    private void setupFooter(XWPFDocument document) {
        XWPFHeaderFooterPolicy policy = document.getHeaderFooterPolicy();
        if (policy == null) {
            policy = document.createHeaderFooterPolicy();
        }

        XWPFFooter footer = policy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);
        List<XWPFParagraph> footerParagraphs = footer.getParagraphs();
        XWPFParagraph paragraph = !footerParagraphs.isEmpty()
                ? footerParagraphs.get(0) : footer.createParagraph();

        // 紧凑间距
        CTPPr fPPr = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();
        CTSpacing fSpacing = fPPr.isSetSpacing() ? fPPr.getSpacing() : fPPr.addNewSpacing();
        fSpacing.setBefore(BigInteger.ZERO);
        fSpacing.setAfter(BigInteger.ZERO);

        paragraph.setAlignment(ParagraphAlignment.RIGHT);

        XWPFRun run = paragraph.createRun();
        setRunFont(run, HEADER_FONT_SIZE, true);
        run.setText("第");

        // PAGE 域
        CTP ctp = paragraph.getCTP();
        CTSimpleField pageField = ctp.addNewFldSimple();
        pageField.setInstr("PAGE");
        CTR pageR = pageField.addNewR();
        setRunFontForCTR(pageR, HEADER_FONT_SIZE, true);
        pageR.addNewT().setStringValue("1");

        XWPFRun run2 = paragraph.createRun();
        setRunFont(run2, HEADER_FONT_SIZE, true);
        run2.setText("页 of ");

        // NUMPAGES 域
        CTSimpleField numPagesField = ctp.addNewFldSimple();
        numPagesField.setInstr("NUMPAGES");
        CTR numR = numPagesField.addNewR();
        setRunFontForCTR(numR, HEADER_FONT_SIZE, true);
        numR.addNewT().setStringValue("1");

        XWPFRun run3 = paragraph.createRun();
        setRunFont(run3, HEADER_FONT_SIZE, true);
        run3.setText("页");
    }

    /**
     * 写入代码内容
     * <p>
     * 所有代码行写入同一个段落，用addBreak()换行。
 * 行距设为精确293 twips，Word自然分页每页50行。
 * 注意：长行截断在 MainController 中预处理完成，此处直接写入。
     * </p>
     */
    private void writeContent(XWPFDocument document, List<String> lines) {
        // 创建段落
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);

        // 设置段落属性 - 精确行距
        CTPPr pPr = paragraph.getCTP().getPPr() != null ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();
        CTSpacing spacing = pPr.isSetSpacing() ? pPr.getSpacing() : pPr.addNewSpacing();
        spacing.setLine(BigInteger.valueOf(LINE_SPACING));
        spacing.setLineRule(STLineSpacingRule.EXACT);
        spacing.setBefore(BigInteger.ZERO);
        spacing.setAfter(BigInteger.ZERO);

        for (int i = 0; i < lines.size(); i++) {
            XWPFRun run = paragraph.createRun();
            setRunFont(run, FONT_SIZE, false);
            run.setText(lines.get(i));

            if (i < lines.size() - 1) {
                run.addBreak();
            }
        }
    }

    /**
     * 设置XWPFRun的字体
     *
     * @param run      运行对象
     * @param fontSize 字号（half-points）
     * @param isBold   是否加粗
     */
    private void setRunFont(XWPFRun run, int fontSize, boolean isBold) {
        // 直接通过XML设置字体，确保字号精确
        CTRPr rPr = run.getCTR().isSetRPr() ? run.getCTR().getRPr() : run.getCTR().addNewRPr();
        // 字体大小
        rPr.addNewSz().setVal(BigInteger.valueOf(fontSize));
        rPr.addNewSzCs().setVal(BigInteger.valueOf(fontSize));
        // 加粗
        rPr.addNewB().setVal(isBold);
        // 中文字体
        CTFonts fonts = rPr.addNewRFonts();
        fonts.setEastAsia(FONT_CN);
        fonts.setAscii(FONT_EN);
        fonts.setHAnsi(FONT_EN);
    }

    /**
     * 设置CTR的字体（用于域代码中的文本）
     *
     * @param ctr      域代码运行对象
     * @param fontSize 字号（half-points）
     * @param isBold   是否加粗
     */
    private void setRunFontForCTR(CTR ctr, int fontSize, boolean isBold) {
        CTRPr rPr = ctr.isSetRPr() ? ctr.getRPr() : ctr.addNewRPr();
        CTFonts fonts = rPr.addNewRFonts();
        fonts.setEastAsia(FONT_CN);
        fonts.setAscii(FONT_EN);
        fonts.setHAnsi(FONT_EN);
        rPr.addNewSz().setVal(BigInteger.valueOf(fontSize));
        rPr.addNewSzCs().setVal(BigInteger.valueOf(fontSize));
        rPr.addNewB().setVal(isBold);
    }
}