package com.copyright.generator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 软著程序鉴别材料生成器 - 主启动类
 * <p>
 * 基于JavaFX的桌面应用程序，用于自动扫描项目源码并生成符合软件著作权申请要求的
 * Word(.docx)格式的程序鉴别材料文档。
 * </p>
 *
 * @author vx公众号：全粘程序员
 * @version 1.0.0
 * @since 2026-08-16
 */
public class MainApp extends Application {

    /** 应用程序标题 */
    private static final String APP_TITLE = "软著程序鉴别材料生成器 v1.0.0";

    /** 默认窗口宽度 */
    private static final int DEFAULT_WIDTH = 900;

    /** 默认窗口高度 */
    private static final int DEFAULT_HEIGHT = 700;

    /**
     * 应用程序入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * JavaFX启动方法，初始化并显示主窗口
     *
     * @param primaryStage 主舞台
     * @throws Exception 加载FXML失败时抛出
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setOnCloseRequest(event -> System.exit(0));
        primaryStage.show();
    }
}