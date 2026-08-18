# 软著程序鉴别材料生成器

自动扫描项目源码，生成符合中国版权保护中心要求的程序鉴别材料 Word 文档。

## 功能

- 递归扫描源代码目录，支持按文件扩展名过滤、忽略指定目录
- 两种生成模式：
  - **全量生成** — 输出全部代码
  - **前30页+后30页** — 提取前1500行和后1500行，满足软著申请最低要求
- 输出 Word(.docx) 文档，规范如下：
  - 每页精确 50 行代码，五号宋体（10.5pt），英文/数字 Times New Roman
  - 页眉：软件名称+版本号 / 程序鉴别材料 / 页码
  - 页脚：第 X 页 of 总页数
  - A4 纸张，自动过滤空行，长行（>90 字符）硬截断防溢出
- 跨平台打开输出目录（macOS Finder 定位文件 / Windows 资源管理器）

## 技术栈

- Java 21
- JavaFX 21（桌面 GUI）
- Apache POI 5.2.5（Word 文档生成）
- Maven 构建

## 环境要求

- JDK 21+
- Maven 3.6+

## 快速开始

```bash
# 克隆项目
git clone <仓库地址>
cd software-copyright-generator

# 编译并运行
mvn javafx:run
```

在 IDEA 中开发时，右键 `AppLauncher.java` → Run 即可直接启动。

## 使用说明

1. 输入软件名称和版本号（必填，用于页眉）
2. 选择源代码目录（必填）
3. 选择输出目录（可选，默认在源码同级创建「输出文档」）
4. 输入文件后缀，空格分隔（如 `java js py`，留空则扫描全部文件）
5. 输入忽略目录，空格分隔（如 `node_modules target .git`）
6. 选择生成模式：全量生成 / 前30页+后30页
7. 点击「开始生成」

## 项目结构

```
src/main/java/com/copyright/generator/
├── MainApp.java              # JavaFX Application 入口
├── AppLauncher.java           # IDEA 直接运行启动器
├── controller/
│   └── MainController.java   # 主界面控制器
├── service/
│   ├── CodeScanner.java      # 源码扫描服务
│   └── WordGenerator.java    # Word 文档生成器
└── util/
    ├── FileUtil.java          # 文件操作工具
    └── PageUtil.java          # 分页工具

src/main/resources/
├── fxml/Main.fxml             # 界面布局
└── css/style.css              # 样式
```