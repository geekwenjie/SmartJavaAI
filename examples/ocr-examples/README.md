# OCR文字识别示例


## 📁 项目结构

```

src
├── main
│   ├── java
│   │   └── smartai/examples/ocr
│   │       ├── common
│   │       │   ├── OcrDetectionDemo.java     # 文本检测示例
│   │       │   ├── OcrDirectionDetDemo.java  # 文本方向检测示例
│   │       │   └── OcrRecognizeDemo.java     # 文本识别示例
│   │       └── table
│   │           └── TableRecDemo.java         # 表格识别示例
│   │       └── plate
│   │           └── PlateRecDemo.java         # 车牌识别示例
│   └── resources
│       ├── logback.xml                       # 日志配置文件
└── test


```


---



## ⚙️ 配置要求

- **运行环境**：
    - JDK 1.8 或更高版本
    - IntelliJ IDEA 推荐作为开发 IDE
- **依赖库**：
    - OpenCV、DJL、SmartJavaAI SDK
- **模型路径**：
    - 所有模型需下载并配置正确的路径（参考各 demo 注释中的链接）

---

## 🚀 快速开始

如果你只想运行某个示例，请按以下方式操作：

1. 打开 IDEA（或你喜欢的 IDE）
2. 选择 **“Open”**，然后仅导入 `examples` 目录下对应的示例项目，例如：

   ```
   examples/ocr-example
   ```
3. IDEA 会自动识别并加载依赖。若首次导入，请等待 Maven 下载依赖完成。
4. 请从我们提供的 百度网盘 中下载模型及其附带文件，并在示例代码中将模型路径修改为您本地的实际路径。
5. 可通过查看每个 Java 文件顶部的注释了解对应功能，或参考 README 文件中对各 Java 文件功能的说明，运行相应的测试方法进行体验。


---

## 📄 文档

有关完整使用说明，请查阅 SmartJavaAI 官方文档：
[http://doc.smartjavaai.cn](http://doc.smartjavaai.cn)

---
