package com.copyright.generator.service;

import com.copyright.generator.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 源码扫描服务
 * <p>
 * 递归扫描指定目录下的源码文件，根据用户指定的文件扩展名过滤，
 * 并排除忽略目录中的文件。返回所有符合条件的代码行列表。
 * </p>
 *
 * @author vx公众号：全粘程序员
 * @version 1.0.0
 * @since 2026-08-16
 */
public class CodeScanner {

    /** 源代码目录 */
    private final File sourceDir;

    /** 文件扩展名过滤列表（不含点号），为空则不过滤 */
    private final List<String> extensions;

    /** 忽略的目录名称列表 */
    private final List<String> ignoreDirs;

    /** 扫描到的文件总数 */
    private int scannedFileCount;

    /** 扫描到的总行数 */
    private int totalLineCount;

    /**
     * 构造源码扫描器
     *
     * @param sourceDir  源代码目录
     * @param extensions 文件扩展名列表（不含点号）
     * @param ignoreDirs 忽略目录列表
     */
    public CodeScanner(File sourceDir, List<String> extensions, List<String> ignoreDirs) {
        this.sourceDir = sourceDir;
        this.extensions = extensions;
        this.ignoreDirs = ignoreDirs;
        this.scannedFileCount = 0;
        this.totalLineCount = 0;
    }

    /**
     * 执行扫描，返回所有符合条件的代码行
     *
     * @return 所有代码行列表
     */
    public List<String> scan() {
        List<String> allLines = new ArrayList<>();
        scanDirectory(sourceDir, allLines);
        return allLines;
    }

    /**
     * 递归扫描目录
     *
     * @param dir      当前目录
     * @param allLines 收集所有代码行的列表
     */
    private void scanDirectory(File dir, List<String> allLines) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 检查是否在忽略目录列表中
                if (!FileUtil.shouldIgnoreDir(file.getName(), ignoreDirs)) {
                    scanDirectory(file, allLines);
                }
            } else if (file.isFile()) {
                // 检查文件扩展名是否匹配
                if (FileUtil.matchesExtension(file.getName(), extensions)) {
                    List<String> lines = FileUtil.readAllLines(file);
                    if (!lines.isEmpty()) {
                        // 添加文件名注释
                        allLines.add("// ====== " + file.getName() + " ======");
                        // 过滤空行，只保留有实际内容的行
                        for (String line : lines) {
                            if (line.trim().length() > 0) {
                                allLines.add(line);
                                totalLineCount++;
                            }
                        }
                        scannedFileCount++;
                    }
                }
            }
        }
    }

    /**
     * 获取扫描到的文件数
     *
     * @return 文件数
     */
    public int getScannedFileCount() {
        return scannedFileCount;
    }

    /**
     * 获取扫描到的总行数
     *
     * @return 总行数
     */
    public int getTotalLineCount() {
        return totalLineCount;
    }
}