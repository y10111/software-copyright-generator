package com.copyright.generator.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件操作工具类
 * <p>
 * 提供文件读写、目录操作等通用工具方法
 * </p>
 *
 * @author vx公众号：全粘程序员
 * @version 1.0.0
 * @since 2026-08-16
 */
public class FileUtil {

    /**
     * 读取文件的所有行
     *
     * @param file 要读取的文件
     * @return 文件中的所有行，读取失败返回空列表
     */
    public static List<String> readAllLines(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("读取文件失败: " + file.getAbsolutePath() + " - " + e.getMessage());
        }
        return lines;
    }

    /**
     * 确保目录存在，不存在则创建
     *
     * @param dir 目录路径
     * @return 目录是否已存在或创建成功
     */
    public static boolean ensureDirectory(File dir) {
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return dir.isDirectory();
    }

    /**
     * 判断目录名是否在忽略列表中
     *
     * @param dirName    目录名
     * @param ignoreDirs 忽略目录列表
     * @return 是否应该忽略
     */
    public static boolean shouldIgnoreDir(String dirName, List<String> ignoreDirs) {
        if (ignoreDirs == null || ignoreDirs.isEmpty()) {
            return false;
        }
        return ignoreDirs.contains(dirName);
    }

    /**
     * 判断文件扩展名是否匹配
     *
     * @param fileName   文件名
     * @param extensions 扩展名列表（不含点号），为空则匹配所有
     * @return 是否匹配
     */
    public static boolean matchesExtension(String fileName, List<String> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            return true;
        }
        String lowerName = fileName.toLowerCase();
        for (String ext : extensions) {
            String extWithDot = ext.startsWith(".") ? ext.toLowerCase() : "." + ext.toLowerCase();
            if (lowerName.endsWith(extWithDot)) {
                return true;
            }
        }
        return false;
    }
}