package com.copyright.generator;

import javafx.application.Application;

/**
 * 启动器 - 解决 IDEA 直接运行 MainApp 时「缺少 JavaFX 运行时组件」的问题
 * <p>
 * JavaFX 11+ 要求模块路径加载，但 IDEA 默认将依赖放在 classpath 上。
 * 如果 main 方法所在的类继承了 Application，JVM 加载类时就会触发检查并报错。
 * 本类不继承 Application，通过反射调用 Application.launch() 绕过检查。
 * </p>
 */
public class AppLauncher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}