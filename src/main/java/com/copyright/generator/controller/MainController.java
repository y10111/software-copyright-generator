package com.copyright.generator.controller;

import com.copyright.generator.service.CodeScanner;
import com.copyright.generator.service.WordGenerator;
import com.copyright.generator.util.FileUtil;
import com.copyright.generator.util.PageUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;

import java.awt.Desktop;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 主界面控制器
 * <p>
 * 负责处理用户界面交互，协调源码扫描、文档生成等服务的调用。
 * 管理日志输出、进度条更新、状态栏等UI反馈。
 * </p>
 *
 * @author vx公众号：全粘程序员
 * @version 1.0.0
 * @since 2026-08-16
 */
public class MainController {

    // ==================== FXML 绑定 ====================

    @FXML private TextField softwareNameField;
    @FXML private TextField versionField;
    @FXML private TextField sourceDirField;
    @FXML private TextField outputDirField;
    @FXML private TextField extensionsField;
    @FXML private TextField ignoreDirsField;
    @FXML private ToggleGroup modeGroup;
    @FXML private RadioButton fullModeRadio;
    @FXML private RadioButton partialModeRadio;
    @FXML private Button generateButton;
    @FXML private Button openOutputDirButton;
    @FXML private ScrollPane logScrollPane;
    @FXML private TextFlow logTextFlow;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Label statusLabel;
    @FXML private Label fileCountLabel;
    @FXML private Label lineCountLabel;

    // ==================== 状态变量 ====================

    /** 当前选中的源码目录 */
    private File selectedSourceDir;

    /** 用户自定义的输出目录，null则使用默认 */
    private File customOutputDir;

    /** 最后一次生成的输出目录，用于打开 */
    private File lastOutputDir;

    /** 最后一次生成的Word文件，用于Finder定位 */
    private File lastWordFile;

    /** 日志时间格式化（ThreadLocal保证线程安全） */
    private static final ThreadLocal<SimpleDateFormat> LOG_TIME_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    // ==================== 初始化 ====================

    /**
     * JavaFX初始化，绑定表单变化监听，表单变化时清零进度条
     */
    @FXML
    private void initialize() {
        // 表单字段变化时清零进度条，用户准备重新生成
        softwareNameField.textProperty().addListener((obs, old, val) -> resetProgress());
        versionField.textProperty().addListener((obs, old, val) -> resetProgress());
        sourceDirField.textProperty().addListener((obs, old, val) -> resetProgress());
        outputDirField.textProperty().addListener((obs, old, val) -> resetProgress());
        extensionsField.textProperty().addListener((obs, old, val) -> resetProgress());
        ignoreDirsField.textProperty().addListener((obs, old, val) -> resetProgress());
        modeGroup.selectedToggleProperty().addListener((obs, old, val) -> resetProgress());
    }

    /**
     * 重置进度条和状态到初始值
     */
    private void resetProgress() {
        progressBar.setProgress(0);
        progressLabel.setText("0%");
        fileCountLabel.setText("--");
        lineCountLabel.setText("--");
        statusLabel.setText("就绪");
    }

    // ==================== 菜单事件 ====================

    @FXML
    private void onImportSource() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择项目源码目录");
        File dir = chooser.showDialog(null);
        if (dir != null) {
            selectedSourceDir = dir;
            sourceDirField.setText(dir.getAbsolutePath());
            sourceDirField.setStyle("");
            // 自动设置默认输出目录
            if (customOutputDir == null) {
                File defaultOutput = new File(dir.getParentFile(), "输出文档");
                outputDirField.setText(defaultOutput.getAbsolutePath());
            }
            setStatus("已选择源码目录: " + dir.getName());
            appendLog("info", "已选择源码目录: " + dir.getAbsolutePath());
        }
    }

    @FXML
    private void onExit() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void onHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("使用说明");
        alert.setHeaderText("软著程序鉴别材料生成器");
        alert.setContentText(
                "【功能简介】\n" +
                "自动扫描项目源码，生成符合中国版权保护中心要求的\n" +
                "程序鉴别材料（Word文档），每页精确50行代码。\n\n" +
                "【使用步骤】\n" +
                "1. 输入软件名称和版本号（*必填，用于页眉）\n" +
                "2. 选择源代码目录（*必填）\n" +
                "3. 选择输出目录（可选，默认同级「输出文档」）\n" +
                "4. 输入文件后缀，空格分隔（如：java js py），\n" +
                "   留空则扫描全部文件\n" +
                "5. 输入忽略目录，空格分隔\n" +
                "   （如：node_modules target .git）\n" +
                "6. 选择生成模式：\n" +
                "   - 全量生成：输出全部代码\n" +
                "   - 前30页+后30页：输出前1500行+后1500行\n" +
                "7. 点击「开始生成」\n\n" +
                "【文档规范】\n" +
                "   - 每页50行，五号宋体(10.5pt)\n" +
                "   - 页眉：软件名称+版本号 / 程序鉴别材料 / 页码\n" +
                "   - 页脚：第X页\n" +
                "   - 自动过滤空行，长行硬截断防溢出\n" +
                "   - 输出格式：Word(.docx)"
        );
        alert.setResizable(true);
        alert.showAndWait();
    }

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText("软著程序鉴别材料生成器");
        alert.setContentText(
                "版本：v1.0.0\n\n" +
                "作者：vx公众号：全粘程序员\n\n" +
                "本工具用于自动生成软件著作权申请所需的\n" +
                "程序鉴别材料，支持全量和前30页+后30页\n" +
                "两种模式，输出符合版权局规范的Word文档。\n\n" +
                "技术栈：Java 21 + JavaFX + Apache POI\n\n" +
                "Copyright © 2026"
        );
        alert.showAndWait();
    }

    @FXML
    private void onSupport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("支持");
        alert.setHeaderText("如果这个工具帮到了你，欢迎请作者喝杯咖啡~");

        ImageView qrCode = new ImageView(new Image(getClass().getResourceAsStream("/qrcode.jpg")));
        qrCode.setFitWidth(220);
        qrCode.setPreserveRatio(true);
        qrCode.setSmooth(true);

        alert.getDialogPane().setContent(qrCode);
        alert.showAndWait();
    }

    // ==================== 按钮事件 ====================

    @FXML
    private void onSelectDirectory() {
        onImportSource();
    }

    /**
     * 选择输出目录
     */
    @FXML
    private void onSelectOutputDir() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择输出目录");
        if (lastOutputDir != null && lastOutputDir.exists()) {
            chooser.setInitialDirectory(lastOutputDir);
        } else if (selectedSourceDir != null) {
            chooser.setInitialDirectory(selectedSourceDir.getParentFile());
        }
        File dir = chooser.showDialog(null);
        if (dir != null) {
            customOutputDir = dir;
            outputDirField.setText(dir.getAbsolutePath());
            setStatus("已选择输出目录: " + dir.getName());
            appendLog("info", "已选择输出目录: " + dir.getAbsolutePath());
        }
    }

    /**
     * 打开输出目录（跨平台定位选中生成的Word文件）
     */
    @FXML
    private void onOpenOutputDir() {
        if (lastOutputDir != null && lastOutputDir.exists()) {
            try {
                if (lastWordFile != null && lastWordFile.exists()) {
                    String os = System.getProperty("os.name").toLowerCase();
                    String filePath = lastWordFile.getAbsolutePath();
                    if (os.contains("mac")) {
                        // macOS: Finder定位并选中文件
                        Runtime.getRuntime().exec(new String[]{"open", "-R", filePath});
                    } else if (os.contains("win")) {
                        // Windows: 资源管理器定位并选中文件
                        Runtime.getRuntime().exec(new String[]{"explorer", "/select,\"" + filePath + "\""});
                    } else {
                        // Linux: 打开目录
                        Desktop.getDesktop().open(lastOutputDir);
                    }
                } else {
                    Desktop.getDesktop().open(lastOutputDir);
                }
                appendLog("info", "已打开输出目录");
            } catch (Exception e) {
                appendLog("error", "无法打开输出目录: " + e.getMessage());
            }
        }
    }

    /**
     * 开始生成文档（验证输入、检查覆盖、后台执行）
     */
    @FXML
    private void onGenerate() {
        // 清除之前的错误样式
        clearErrorStyles();

        // 验证输入
        if (!validateInput()) {
            return;
        }

        // 获取参数
        String softwareName = softwareNameField.getText().trim();
        String version = versionField.getText().trim();

        // 检查输出文件是否已存在，提示用户确认覆盖
        String safeName = softwareName.replaceAll("[\\\\/:*?\"<>|]", "_");
        File outputDir = getOutputDir();
        File wordFile = new File(outputDir, safeName + "_" + version + "_程序鉴别材料.docx");

        if (wordFile.exists()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("文件已存在");
            confirm.setHeaderText("输出文件已存在");
            confirm.setContentText(wordFile.getName() + "\n\n是否覆盖？");

            Optional<ButtonType> result = confirm.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return;
            }
            appendLog("warn", "文件已存在，用户选择覆盖");
        }

        // 禁用按钮
        generateButton.setDisable(true);
        openOutputDirButton.setVisible(false);

        boolean isFullMode = fullModeRadio.isSelected();
        List<String> extensions = parseSpaceSeparated(extensionsField.getText());
        List<String> ignoreDirs = parseSpaceSeparated(ignoreDirsField.getText());

        setStatus("正在生成...");

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                executeGeneration(softwareName, version, isFullMode, extensions, ignoreDirs);
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            generateButton.setDisable(false);
            openOutputDirButton.setVisible(true);
            progressBar.setProgress(1.0);
            progressLabel.setText("100%");
            setStatus("生成完成");
            appendLog("info", "========== end ==========");
        });

        task.setOnFailed(event -> {
            generateButton.setDisable(false);
            setStatus("生成失败");
            Throwable ex = task.getException();
            appendLog("error", "生成失败: " + ex.getMessage());
            ex.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // ==================== 核心逻辑 ====================

    private void executeGeneration(String softwareName, String version, boolean isFullMode,
                                   List<String> extensions, List<String> ignoreDirs) throws Exception {
        // 阶段1: 扫描源码
        appendLog("info", "========== 开始扫描 " + softwareName + " " + version + " 源码 ==========");
        appendLog("info", "源码目录: " + selectedSourceDir.getAbsolutePath());
        appendLog("info", "文件后缀: " + (extensions.isEmpty() ? "全部" : String.join(", ", extensions)));
        appendLog("info", "忽略目录: " + (ignoreDirs.isEmpty() ? "无" : String.join(", ", ignoreDirs)));

        setStatus("正在扫描源码...");
        updateProgress(0, 0);

        CodeScanner scanner = new CodeScanner(selectedSourceDir, extensions, ignoreDirs);
        List<String> allLines = scanner.scan();

        appendLog("info", "扫描完成: " + scanner.getScannedFileCount() + " 个文件, "
                + scanner.getTotalLineCount() + " 行有效代码");

        // 更新界面扫描结果
        String fileInfo = "共 " + scanner.getScannedFileCount() + " 个文件";
        String lineInfo = "有效代码 " + scanner.getTotalLineCount() + " 行";
        Platform.runLater(() -> {
            fileCountLabel.setText(fileInfo);
            lineCountLabel.setText(lineInfo);
        });

        if (allLines.isEmpty()) {
            appendLog("warn", "未扫描到任何代码，请检查目录和文件后缀设置");
            setStatus("扫描完成，无代码");
            return;
        }

        // 阶段1.5: 长行硬截断
        int originalLineCount = allLines.size();
        allLines = expandLongLines(allLines);
        int expandedCount = allLines.size() - originalLineCount;
        if (expandedCount > 0) {
            appendLog("warn", "检测到 " + expandedCount + " 处长行，已硬截断(>90字符/行)");
        }

        updateProgress(0.15, 15);

        // 阶段2: 处理生成模式
        List<String> linesToGenerate;
        int totalPages;

        if (isFullMode) {
            linesToGenerate = allLines;
            totalPages = PageUtil.calculateTotalPages(allLines.size());
            appendLog("info", "生成模式: 全量生成");
            appendLog("info", "总行数: " + allLines.size() + " 行, 共 " + totalPages + " 页");
        } else {
            linesToGenerate = PageUtil.getPartialLines(allLines);
            totalPages = PageUtil.calculateTotalPages(linesToGenerate.size());
            appendLog("info", "生成模式: 前30页+后30页");
            appendLog("info", "源程序量: " + scanner.getTotalLineCount() + " 行 (有效代码行数)");
            appendLog("info", "提取行数: " + linesToGenerate.size() + " 行, 共 " + totalPages + " 页");
        }

        updateProgress(0.25, 25);

        // 阶段3: 确定输出目录
        setStatus("正在生成文档...");
        File outputDir = getOutputDir();
        FileUtil.ensureDirectory(outputDir);
        lastOutputDir = outputDir;
        appendLog("info", "输出目录: " + outputDir.getAbsolutePath());

        updateProgress(0.30, 30);

        // 阶段4: 生成Word文档
        appendLog("info", "正在生成Word文档...");
        String safeName = softwareName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String wordFileName = safeName + "_" + version + "_程序鉴别材料.docx";
        File wordFile = new File(outputDir, wordFileName);

        WordGenerator wordGenerator = new WordGenerator(softwareName, version);
        wordGenerator.generate(wordFile, linesToGenerate);
        lastWordFile = wordFile;
        appendLog("info", "Word文档已生成: " + wordFileName);

        updateProgress(0.95, 95);

        appendLog("info", "========== " + softwareName + " " + version + " 全部文档生成完毕 ==========");
        appendLog("info", "输出目录: " + outputDir.getAbsolutePath());
        appendLog("info", "总页数: " + totalPages + " 页 (每页50行)");
        appendLog("info", "源程序量: " + scanner.getTotalLineCount() + " 行 (用于软著申请表填写)");
        setStatus("生成完成 - 输出目录: " + outputDir.getAbsolutePath());
    }

    // ==================== 工具方法 ====================

    /**
     * 验证用户输入，无效字段标红
     */
    private boolean validateInput() {
        boolean valid = true;

        if (softwareNameField.getText() == null || softwareNameField.getText().trim().isEmpty()) {
            softwareNameField.getStyleClass().add("error");
            valid = false;
        }

        if (versionField.getText() == null || versionField.getText().trim().isEmpty()) {
            versionField.getStyleClass().add("error");
            valid = false;
        }

        if (selectedSourceDir == null || !selectedSourceDir.exists()) {
            sourceDirField.getStyleClass().add("error");
            valid = false;
        }

        if (!valid) {
            setStatus("请填写所有必填项（标*的字段）");
            appendLog("warn", "请填写所有必填项（软件名称、版本号、源码目录）");
        }

        return valid;
    }

    /**
     * 清除输入框的错误样式
     */
    private void clearErrorStyles() {
        softwareNameField.getStyleClass().remove("error");
        versionField.getStyleClass().remove("error");
        sourceDirField.getStyleClass().remove("error");
    }

    /**
     * 获取输出目录，优先用户自定义，否则默认源码同级"输出文档"
     */
    private File getOutputDir() {
        if (customOutputDir != null) {
            return customOutputDir;
        }
        return new File(selectedSourceDir.getParentFile(), "输出文档");
    }

    /**
     * 更新状态栏文字（线程安全）
     */
    private void setStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    /**
     * 解析空格分隔的字符串为列表
     */
    private List<String> parseSpaceSeparated(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(text.trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 添加日志（带时间戳和颜色）
     */
    private void appendLog(String level, String message) {
        Platform.runLater(() -> {
            String timestamp = LOG_TIME_FORMAT.get().format(new Date());
            Text text = new Text("[" + timestamp + "] [" + level.toUpperCase() + "] " + message + "\n");

            switch (level) {
                case "info":
                    text.setFill(Color.GREEN);
                    break;
                case "warn":
                    text.setFill(Color.ORANGE);
                    break;
                case "error":
                    text.setFill(Color.RED);
                    break;
                default:
                    text.setFill(Color.WHITE);
                    break;
            }

            logTextFlow.getChildren().add(text);
        });
    }

    /**
     * 更新进度条（线程安全）
     */
    private void updateProgress(double progress, int percentage) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            progressLabel.setText(percentage + "%");
        });
    }

    private List<String> expandLongLines(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            if (line.length() > 90) {
                int pos = 0;
                while (pos < line.length()) {
                    int end = Math.min(pos + 90, line.length());
                    result.add(line.substring(pos, end));
                    pos = end;
                }
            } else {
                result.add(line);
            }
        }
        return result;
    }
}