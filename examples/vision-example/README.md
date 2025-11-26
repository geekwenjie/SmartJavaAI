# 计算机视觉示例

本项目包含了多个计算机视觉相关的示例代码，展示了如何使用 SmartJavaAI SDK 进行各种视觉任务。

## 📁 项目结构

```
vision-example/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── smartai/examples/vision/
│   │   │       ├── ActionRecognizeDemo.java    # 动作识别示例
│   │   │       ├── ClipDemo.java               # CLIP模型示例（图文匹配）
│   │   │       ├── ClsDemo.java                # 图像分类示例
│   │   │       ├── InstanceSegDemo.java        # 实例分割示例
│   │   │       ├── ObbDetDemo.java             # 旋转框检测示例
│   │   │       ├── ObjectDetectionDemo.java    # 目标检测示例
│   │   │       ├── PersonDetectDemo.java       # 行人检测示例
│   │   │       ├── PoseDetDemo.java            # 姿态检测示例
│   │   │       ├── SemSegDemo.java             # 语义分割示例
```

## 🧩 功能模块说明

### 1. 目标检测 [ObjectDetectionDemo.java]
- **功能**：展示了如何使用目标检测模型进行物体检测
- **特点**：支持图片检测、视频流检测、本地摄像头检测等多种场景

### 2. 动作识别 [ActionRecognizeDemo.java]
- **功能**：展示了如何使用动作识别模型识别图片或视频中的人物动作
- **特点**：支持多种动作类别的识别

### 3. CLIP模型 [ClipDemo.java]
- **功能**：展示了如何使用CLIP模型进行图文匹配相关任务
- **特点**：支持图片特征提取、文本特征提取、相似度计算、图文匹配等功能

### 4. 实例分割 [InstanceSegDemo.java]
- **功能**：展示了如何使用实例分割模型进行物体分割
- **特点**：可以精确分割出图片中的每个物体实例

### 5. 旋转框检测 [ObbDetDemo.java]
- **功能**：展示了如何使用旋转框检测模型检测任意角度的物体
- **特点**：适用于航拍图片等场景中的物体检测

### 6. 行人检测 [PersonDetectDemo.java]
- **功能**：展示了如何使用专门的行人检测模型进行人体检测
- **特点**：针对行人检测场景优化，准确率更高

### 7. 姿态检测 [PoseDetDemo.java]
- **功能**：展示了如何使用姿态检测模型检测人体关键点
- **特点**：可以识别人体的各个关节点位置

### 8. 语义分割 [SemSegDemo.java]
- **功能**：展示了如何使用语义分割模型进行场景分割
- **特点**：可以对图片中的每个像素进行分类

### 9. 图像分类 [ClsDemo.java]
- **功能**：展示了如何使用分类模型对图片进行分类
- **特点**：支持多种分类模型和类别

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

## 运行方式

如果你只想运行某个示例，请按以下方式操作：

1. 打开 IDEA（或你喜欢的 IDE）
2. 选择 **“Open”**，然后仅导入 `examples` 目录下对应的示例项目，例如：

   ```
   examples/vison-example
   ```
3. IDEA 会自动识别并加载依赖。若首次导入，请等待 Maven 下载依赖完成。
4. 请从我们提供的 百度网盘 中下载模型及其附带文件，并在示例代码中将模型路径修改为您本地的实际路径。
5. 可通过查看每个 Java 文件顶部的注释了解对应功能，或参考 README 文件中对各 Java 文件功能的说明，运行相应的测试方法进行体验。


---

## 📄 文档

有关完整使用说明，请查阅 SmartJavaAI 官方文档：
[http://doc.smartjavaai.cn](http://doc.smartjavaai.cn)

---
