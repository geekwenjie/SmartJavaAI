# SmartJavaAI 示例项目说明


## 项目结构

```
src/main/java/smartai/examples/
├── face-example/                    人脸检测、人脸识别等功能示例
├── vision-example/                  通用视觉检测示例：目标检测、目标分割、图像分类等
├── ocr-example/                     OCR文字识别、车牌识别等功能示例
├── translate-example/               机器翻译功能示例
├── speech-example/                  语音识别、语音合成功能示例

```

本项目在 `examples` 文件夹下提供了多个示例工程，用于演示各功能模块的使用方法：

* `face-example`：人脸检测、人脸识别等功能示例
* `vision-example`：通用视觉检测示例：目标检测、目标分割、图像分类等
* `ocr-example`：OCR文字识别、车牌识别等功能示例
* `translate-example`：机器翻译功能示例
* `speech-example`：语音识别、语音合成功能示例

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


## 联系与支持

如需帮助或有建议欢迎通过 Issue 反馈或联系作者。
