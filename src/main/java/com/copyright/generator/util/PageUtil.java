package com.copyright.generator.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页工具类
 * <p>
 * 负责将源代码行按每页固定行数进行分页处理。
 * 支持全量分页和前后各取指定页数两种模式。
 * </p>
 *
 * @author vx公众号：全粘程序员
 * @version 1.0.0
 * @since 2026-08-16
 */
public class PageUtil {

    /** 每页行数 */
    public static final int LINES_PER_PAGE = 50;

    /** 前/后各取的页数 */
    public static final int PARTIAL_PAGE_COUNT = 30;

    /** 前/后各取的行数 (30页 × 50行) */
    public static final int PARTIAL_LINE_COUNT = PARTIAL_PAGE_COUNT * LINES_PER_PAGE;

    /**
     * 获取前30页和后30页的代码行（用于部分生成模式）
     * <p>
     * 直接取前1500行和后1500行，精确控制为60页×50行=3000行。
     * 不经过分页，避免最后一页不满50行导致总行数不足3000。
     * 如果总行数不足3000行，则返回全部。
     * </p>
     *
     * @param allLines 所有代码行
     * @return 需要输出的代码行，精确3000行或全部
     */
    public static List<String> getPartialLines(List<String> allLines) {
        int totalLines = allLines.size();

        // 不足3000行(60页)，返回全部
        if (totalLines <= PARTIAL_LINE_COUNT * 2) {
            return new ArrayList<>(allLines);
        }

        List<String> result = new ArrayList<>(PARTIAL_LINE_COUNT * 2);
        // 精确前1500行
        result.addAll(allLines.subList(0, PARTIAL_LINE_COUNT));
        // 精确后1500行
        result.addAll(allLines.subList(totalLines - PARTIAL_LINE_COUNT, totalLines));

        return result;
    }

    /**
     * 计算总页数
     *
     * @param totalLines 总行数
     * @return 总页数
     */
    public static int calculateTotalPages(int totalLines) {
        return (int) Math.ceil((double) totalLines / LINES_PER_PAGE);
    }
}