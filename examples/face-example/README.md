# 人脸识别示例

本项目提供了一系列关于人脸识别相关功能的 Java 示例代码，适用于图像处理、人脸检测、活体检测等场景。所有示例基于 SmartJavaAI 的 SDK 实现。

## 📁 项目结构

```
src/main/java/smartai/examples/face/
├── attribute/          # 人脸属性检测模块
│   └── FaceAttributeDetDemo.java     # 检测性别、年龄等人脸属性
├── expression/         # 表情识别模块
│   └── ExpressionRecDemo.java        # 识别中性、高兴、悲伤等7种表情
├── facedet/            # 人脸检测模块
│   └── FaceDetDemo.java              # 检测图片或视频中的人脸并绘制人脸框
├── facerec/            # 人脸识别模块（1:1, 1:N）
│   └── FaceRecDemo.java              # 提取人脸特征、比对、注册与搜索人脸库
├── liveness/           # 活体检测模块
│   └── LivenessDetDemo.java          # 判断是否为真人（静态图或摄像头视频流）
├── quality/            # 人脸质量评估模块
│   └── FaceQualityDetDemo.java       # 评估亮度、清晰度、完整性、姿态、分辨率
└── ViewerFrame.java    # 图像显示窗口工具类（用于在 GUI 中展示图像）
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


## 运行方式

如果你只想运行某个示例，请按以下方式操作：

1. 打开 IDEA（或你喜欢的 IDE）
2. 选择 **“Open”**，然后仅导入 `examples` 目录下对应的示例项目，例如：

   ```
   examples/face-example
   ```
3. IDEA 会自动识别并加载依赖。若首次导入，请等待 Maven 下载依赖完成。
4. 请从我们提供的 百度网盘 中下载模型及其附带文件，并在示例代码中将模型路径修改为您本地的实际路径。
5. 可通过查看每个 Java 文件顶部的注释了解对应功能，或参考 README 文件中对各 Java 文件功能的说明，运行相应的测试方法进行体验。


---

## 📄 文档

有关完整使用说明，请查阅 SmartJavaAI 官方文档：
[http://doc.smartjavaai.cn](http://doc.smartjavaai.cn)

---
