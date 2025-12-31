<p align="center">
	<a href="https://gitee.com/dengwenjie/SmartJavaAI"><img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/logo.png?v=2025-04-13T07:48:42.197Z" width="45%"></a>
</p>
<p align="center">
	<strong>🍬Java轻量级、免费、离线AI工具箱，致力于帮助Java开发者零门槛使用AI算法模型</strong><br>
	<em>像Hutool一样简单易用的Java AI工具箱</em>
</p>
<p align="center">
	👉 <a href="http://smartjavaai.cn/">http://smartjavaai.cn/</a> 👈
</p>
<p align="center">
	<a target="_blank" href="https://central.sonatype.com/artifact/ink.numberone/smartjavaai-all">
		<img src="https://img.shields.io/maven-central/v/ink.numberone/smartjavaai-all.svg?label=Maven%20Central" />
	</a>
	<a target="_blank" href="https://license.coscl.org.cn/MulanPSL2">
		<img src="https://img.shields.io/:license-MulanPSL2-blue.svg" />
	</a>
	<a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk11-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8+-green.svg" />
	</a>
	<a target="_blank" href='https://gitee.com/dengwenjie/SmartJavaAI/stargazers'>
		<img src='https://gitee.com/dengwenjie/SmartJavaAI/badge/star.svg?theme=gvp' alt='star'/>
	</a>
    <a target="_blank" href='https://github.com/geekwenjie/SmartJavaAI'>
		<img src="https://img.shields.io/github/stars/geekwenjie/SmartJavaAI.svg?style=social" alt="github star"/>
	</a>
    <a target="_blank" href='https://gitcode.com/geekwenjie/SmartJavaAI'>
		<img src="https://gitcode.com/geekwenjie/SmartJavaAI/star/badge.svg" alt="gitcode star"/>
	</a>
</p>

-------------------------------------------------------------------------------

[**开发文档**](http://doc.smartjavaai.cn)

-------------------------------------------------------------------------------

## 📚简介

SmartJavaAI是专为JAVA 开发者打造的一个功能丰富、开箱即用的 JAVA AI算法工具包，致力于帮助JAVA开发者零门槛使用各种AI算法模型，开发者无需深入了解底层实现，即可轻松在 Java 代码中调用人脸识别、目标检测、OCR 等功能。底层支持包括基于 DJL (Deep Java Library) 封装的深度学习模型，以及通过 JNI 接入的 C++/Python 算法，兼容多种主流深度学习框架如 PyTorch、TensorFlow、ONNX、Paddle 等，屏蔽复杂的模型部署与调用细节，开发者无需了解 AI 底层实现即可直接在 Java 项目中集成使用，后续将持续扩展更多算法，目标是构建一个“像 Hutool 一样简单易用”的 JAVA AI 通用工具箱


## 📱 SmartJavaAI Android 商业版

**SmartJavaAI 现已支持 Android 移动端！**

如果您有移动端离线人脸识别的需求，我们推出了基于 SmartJavaAI 的 Android SDK 及演示 APP。

* 🚀 **核心能力**：毫秒级离线人脸检测、比对、注册与 1:N 搜索。
* 📦 **开箱即用**：提供标准 SDK 接口与完整 Demo APK。
* 💼 **商业授权**：Android 版本为商业授权版本。

👉 **[点击查看 Android 版演示截图、APK 下载及获取方式](./android.md)**




## 🚀  能力展示

<div align="center">
  <table>      
    <tr>
      <td>
        <div align="left">
          <p>人脸检测(Face Detection)</p>   
         - 5点人脸关键点定位 <br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/face5point.png" width = "500px"/>
        </div>
      </td>
    </tr>          
    <tr>
      <td>
        <div align="left">
          <p>人脸比对1：1<br>(Face Recognition)</p>
          - 人脸对齐 <br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/face1-1.jpg" width = "500px"/>
        </div>
      </td>
    </tr>  
    <tr>
      <td>
        <div align="left">
          <p>人证核验<br>(Face ID Verification)</p>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/idcard.png" width = "500px"/>
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <div align="left">
          <p>人脸比对1：N<br>(Face Recognition)</p>  
          - 人脸对齐 <br>
          - 人脸注册 <br>
          - 人脸库查询<br>
          - 人脸库删除<br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/face1-n.png" width = "500px"/>
        </div>
      </td>
    </tr>  
    <tr>
      <td>
        <div align="left">
          <p>人脸属性检测</p>  
          - 性别检测(GenderDetection) <br>
          - 年龄检测(AgeDetection) <br>
          - 口罩检测(Face Mask Detection)<br>
          - 眼睛状态检测(EyeClosenessDetection)<br>
          - 脸部姿态检测(FacePoseEstimation)<br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/face_attribute.jpg" width = "500px"/>
        </div>
      </td>
    </tr> 
    <tr>
      <td>
        <div align="left">
          <p>活体检测<br>(FaceLivenessDetection)</p>  
          - 图片和视频活体检测 <br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/liveness2.jpg" width = "500px"/>
        </div>
      </td>
    </tr> 
  <tr>
      <td>
        <div align="left">
          <p>人脸表情识别<br>(FacialExpressionRecognition)</p>  
          - 7种表情检测 <br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/emotion.jpg" width = "500px"/>
        </div>
      </td>
    </tr> 
  <tr>
      <td>
        <div align="left">
          <p>图像分类<br>(Image Classification)</p>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/vision/cls.png" height = "300px"/>
        </div>
      </td>
    </tr>
   <tr>
      <td>
        <div align="left">
          <p>目标检测(Object Detection)</p>
          - 视频流目标检测：rtsp、摄像头、视频文件等 <br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/objectdect/object_detect_1.jpeg" width = "500px"/>
        </div>
      </td>
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/objectdect/object_detection_detected.jpg" width = "500px"/>
        </div>
      </td>
    </tr>
 <tr>
      <td>
        <div align="left">
          <p>语义分割<br>(Semantic Segmentation)</p>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/objectdect/dog_bike_car_semseg.png" height = "300px"/>
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <div align="left">
          <p>实例分割<br>(Instance Segmentation)</p>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/objectdect/dog_bike_car_instance.png" height = "300px"/>
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <div align="left">
          <p>自定义目标训练+检测<br>(Object Detection)</p>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/objectdect_train/result.jpg" height = "300px"/>
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <div align="left">
          <p>行人检测(Person Detection)</p>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/objectdect/person_detected.png" height = "300px"/>
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <div align="left">
          <p>人类动作识别<br>(Action Recognition)</p>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/objectdect/calling.jpg" height = "300px"/>
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <div align="left">
          <p>OBB旋转框检测<br>(Oriented Bounding Boxes Object Detection)</p>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/objectdect/boats_detected.jpg" height = "200px"/>
        </div>
      </td>
    </tr>
 <tr>
      <td>
        <div align="left">
          <p>零样本目标检测<br>(ZeroShot Object Detection)</p>
      - YOLO-World 模型 <br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/vision/yolo-world.png" height = "200px"/>
        </div>
      </td>
    </tr>
  <tr>
      <td>
        <div align="left">
          <p>姿态估计<br>(Pose Estimation)</p>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/objectdect/pose_detected.png" height = "300px"/>
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <div align="left">
          <p>OCR文字识别<br>(Text Recognition)</p>
          - 支持任意角度文字识别 <br>
          - 支持印刷体识别 <br>
          - 支持手写字识别<br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/ocr/general_ocr_002_recognized.png" width = "500px"/>
        </div>
      </td>
    </tr> 
  <tr>
      <td>
        <div align="left">
          <p>OCR文字识别</p>
          - 表格识别<br>(Table Structure Recognition) <br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/ocr/table.jpg" width = "500px"/>
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <div align="left">
          <p>车牌识别<br>(License Plate Recognition)</p>
          - 单层/双层检测 <br>
          - 车牌颜色识别 <br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/ocr/plate_recognized.jpg" width = "500px"/>
        </div>
      </td>
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/ocr/plate_recognized2.jpg" width = "500px"/>
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <div align="left">
          <p>机器翻译<br>(Machine Translation)</p>
          - 200多种语言互相翻译
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/translate/translate.png" width = "500px"/>
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <div align="left">
          <p>语音识别<br>(Speech Recognition)</p>
          - 支持100种语言<br>
          - 支持实时语音识别
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/speech/asr.png" width = "500px"/>
        </div>
      </td>
    </tr>
  <tr>
      <td>
        <div align="left">
          <p>语音合成(ASR)</p>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/speech/tts.jpg" width = "500px"/>
        </div>
      </td>
    </tr>
  <tr>
      <td>
        <div align="left">
          <p>CLIP</p>
          - 文搜图 <br>
          - 图搜图 <br>
          - 图搜文<br>
        </div>
      </td>     
      <td>
        <div align="center">
        <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/vision/clip.png" width = "500px"/>
        </div>
      </td>
    </tr>
  </table>
</div>



## 🚀  SmartJavaAI 解决了什么问题？

针对 Java 开发者在集成智能算法时常见的两大痛点：

- 🐍 主流AI深度学习框架（PyTorch/TensorFlow）的Python生态与Java工程体系割裂

- ⚙️ 现有AI集成方案分散杂乱，封装不统一，使用门槛高，不易直接服务于 Java 业务开发

我们实现了：

✅ **开箱即用** - 两行代码即可调用算法

✅ **支持多种深度学习引擎** - Pytorch、Tensorflow、MXNet、ONNX Runtime

✅ **功能丰富** - 当前支持人脸识别、目标检测、OCR、NLP 等多个 AI 领域任务，构建全面的智能算法体系。

✅ **跨平台兼容** - 支持Windows/Linux/macOS系统（x86 & ARM架构）



## 📌 支持功能

- **人脸识别**
  - 人脸检测：人脸区域检测、5点人脸关键点定位
  - 人脸识别：人脸512维特征提取、人脸对齐、1:1 人脸比对、1:N 人脸识别
  - 人脸库：人脸注册、更新、查询、删除(支持向量数据库milvus/sqlite)
  - 人脸属性检测：性别、年龄、口罩、眼睛状态、脸部姿态
  - 静默活体检测：图片、视频活体检测
  - 人脸表情识别：7种表情识别
  - 人脸质量评估：亮度评估、清晰度评估、完整度评估、姿态评估、遮挡评估
- **图像分类**
  - 支持多种主流模型：集成 YOLOv8、YOLOv11 等分类模型
  - 支持自定义模型加载：可无缝加载并部署用户自行训练的分类模型
- **目标检测**
  - 支持多种主流模型：集成 YOLOv5、YOLOv8、YOLOv11、YOLOv12、Tensorflow Object Detection 等目标检测模型
  - 支持自定义模型加载：可无缝加载并部署用户自行训练的目标检测模型
  - 集成行人检测模型
- **语义分割**
  - 集成DeepLabV3模型
- **实例分割**
  - 集成YOLOv8-seg、YOLOv11-seg、Mask R-CNN等模型
- **OBB旋转框目标检测**
  - 集成YOLOv11-obb模型
- **动作识别**
  - 支持KINETICS400数据集中400个人类动作识别
- **姿态估计**
  - 集成YOLOv8-pose、YOLOv11-pose等模型
- **零样本目标检测**
  - 集成YOLOv8s_worldv2、owlv2_base_patch16模型
- **CLIP**
  - 支持提取图片及文本特征
  - 支持文搜图、图搜文、图搜图
- **OCR文字识别**
  - 支持PaddleOCR 3.0模型：集成最新PP-OCRv5、PP-OCRv4、表格结构识别模型(SLANet_plus)、文本行方向分类模型
  - 支持任意角度识别，方向校准
  - 支持通用文字识别，通用手写字识别
  - 支持表格识别
  - 支持中文车牌识别：单层/双层检测，颜色识别，支持12种中文车牌
- **机器翻译**
  - 集成NLLB-200模型：支持200+语言互相翻译
- **语音识别(ASR)**
  - 集成openai的whisper模型：支持100种语言
  - 集成vosk语音识别
  - 集成sherpa-onnx语音识别
- **语音合成(TTS)**
  - 集成sherpa-onnx：支持中文、方言、粤语、英文、德语等多种语言


## 🌟 AI集成方式对比

| 方案                | 技术特点                                                                 | 优点                                                              | 缺点                                            |
|---------------------|--------------------------------------------------------------------------|-------------------------------------------------------------------|-----------------------------------------------|
| **OpenCV**         | 传统图像处理方案                                                        | ✅ 提供java接口<br>✅ 轻量级部署<br>✅ 社区资源丰富                 | ❌ 基于传统算法精度低(60%-75%)<br>❌ 需本地安装环境             |
| &zwnj;**商业闭源SDK（如虹软等）**&zwnj;       | 商业级闭源解决方案                                                      | ✅ 开箱即用<br>✅ 提供完整文档和SDK<br>✅ 支持离线活体检测      | ❌ 免费版需年度授权更新<br>❌ 商业授权费用高<br>❌ 代码不可控          |
| &zwnj;**云API(阿里云)**&zwnj;   | SaaS化云端服务                                                          | ✅ 零部署成本<br>✅ 支持高并发<br>✅ 自带模型迭代        | ❌ 网络延迟风险(200-800ms)<br>❌ 按调用量计费<br>❌ 有数据安全风险  |
| &zwnj;**Python混合调用**&zwnj;  | 跨语言调用方案                                                          | ✅ 可集成PyTorch/TF等框架<br>✅ 支持自定义算法<br>✅ 识别精度高  | ❌ 需维护双语言环境<br>❌ 进程通信性能损耗(30%+)<br>❌ 异常处理复杂度翻倍 |
| &zwnj;**JNI/JNA**&zwnj;         | 跨语言底层调用方案      | ✅ 直接调用 C/C++ 高性能算法库<br>✅ 支持调用各种原生成熟库<br>✅ 可封装成通用工具Jar | ❌ 开发成本高，JNI更复杂<br>❌ 跨平台兼容性差                       |
| &zwnj;**DJL框架**&zwnj;         | 深度学习框架                                                            | ✅ 纯Java实现<br>✅ 支持主流深度学习框架<br>✅ 可加载预训练模型(99%+)   | ❌ 需掌握DL知识<br>❌ 需处理模型加载、预处理、后处理等复杂技术细节         |
| &zwnj;**SmartJavaAI**&zwnj;       | java深度学习工具包                                                            | ✅ 支持主流深度学习框架<br>✅ 提供丰富、开箱即用API<br>✅ 上手简单，单一Jar包集成 | 无                                             |



## 🛠️包含组件

| 模块                    | 介绍                        |
|-----------------------|---------------------------|
| common                | 基础通用模块，封装了公共功能，供各算法模块共享使用 |
| bom                   | 依赖管理模块                    |
| face                  | 人脸功能模块                    |
| vision                | 通用视觉模块(目标检测等功能)           |
| ocr       | OCR文字识别模块                 |
| translate | 机器翻译模块                    |
| speech    | 语音功能模块，包含 ASR 和 TTS       |

可以根据需求对每个模块单独引入，也可以通过引入`all`方式引入所有模块。

-------------------------------------------------------------------------------


## SmartJavaAI 架构图

 <img src="https://cdn.jsdelivr.net/gh/geekwenjie/SmartJavaAI-Site/images/jgt.png" width = "600px"/>

-------------------------------------------------------------------------------

## 📦 安装


### 1、环境要求

- Java 版本：**JDK 8或更高版本**
- 操作系统：不同模型支持的系统不一样，具体请查看[文档](http://doc.smartjavaai.cn)

### 2、Maven

在项目的 `pom.xml` 的 `dependencies` 中可以一次性引入全部功能（如下所示）。
 
⚠️ **注意：不推荐直接引入全部依赖**，更推荐根据实际需求，按功能模块单独引入，避免引入不必要的包。

详细引入方式请查看 [文档](http://doc.smartjavaai.cn/install.html)、或查看[示例代码](https://gitee.com/dengwenjie/SmartJavaAI/tree/master/examples)

```xml
<dependency>
    <groupId>cn.smartjavaai</groupId>
    <artifactId>all</artifactId>
    <version>1.1.1</version>
</dependency>
```

### 3、完整示例代码

[示例代码](https://gitee.com/dengwenjie/SmartJavaAI/tree/master/examples)

本项目在 `examples` 文件夹下提供了多个示例工程，用于演示各功能模块的使用方法：

* `face-example`：人脸检测、人脸识别等功能示例
* `vision-example`：通用视觉检测示例：目标检测、目标分割、图像分类等
* `ocr-example`：OCR文字识别、车牌识别等功能示例
* `translate-example`：机器翻译功能示例
* `speech-example`：语音识别、语音合成功能示例

#### 运行方式

如果你只想运行某个示例，请按以下方式操作：

1. 打开 IDEA（或你喜欢的 IDE）
2. 选择 **“Open”**，然后仅导入 `examples` 目录下对应的示例项目，例如：

   ```
   examples/face-example
   ```
3. IDEA 会自动识别并加载依赖。若首次导入，请等待 Maven 下载依赖完成。
4. 请从我们提供的 百度网盘 中下载模型及其附带文件，并在示例代码中将模型路径修改为您本地的实际路径。
5. 可通过查看每个 Java 文件顶部的注释了解对应功能，或参考 README 文件中对各 Java 文件功能的说明，运行相应的测试方法进行体验。



### 4、文档地址

[开发文档](http://doc.smartjavaai.cn)

### 5、模型简介及下载

[模型下载](https://pan.baidu.com/s/1dlZxWEMULnaietMDUJh38g?pwd=1234)

#### 人脸模块

**人脸检测模型(FaceDetection、FaceLandmarkExtraction)**

| 模型名称                         | 引擎          | 模型简介                         | 模型开源网站                                                                          |
|------------------------------|-------------|------------------------------|---------------------------------------------------------------------------------|
| MTCNN(均衡)                    | PyTorch     | 使用最广泛的模型之一，经典多阶段人脸检测，速度与精度均衡 | [Github](https://github.com/timesler/facenet-pytorch)                           |
| SeetaFace6(均衡模型)                 | C++         | 中科视拓最新开放的开源免费的全栈人脸识别工具包      | [Github](https://github.com/seetafaceengine/SeetaFace6)                         |
| RetinaFace(高精度)              | PyTorch     | 基于单阶段检测的多尺度人脸检测                    | [Github](https://github.com/biubug6/Pytorch_Retinaface)                         |
| RetinaFace_1080x720(高精度)     | OnnxRuntime | 针对高分辨率图像优化的 RetinaFace 模型                    | [Github](https://github.com/biubug6/Pytorch_Retinaface)                         |
| RetinaFace_640x640(高精度)      | OnnxRuntime     | 针对低分辨率图像优化的 RetinaFace 模型                   | [Github](https://github.com/biubug6/Pytorch_Retinaface)                         |
| yolov5face_n_0.5_320x320(极速) | OnnxRuntime     | YOLOv5 小型人脸检测模型，设计轻量，适合快速推理                    | 无                                                                               |
| yolov5face_m_640x640(极速)     | OnnxRuntime     | YOLOv5 中型人脸检测模型                  | 无                         |
| UltraLightFastGenericFace(极速) | PyTorch     | 针对边缘计算设备设计的轻量人脸检测模型          | [Github](https://github.com/Linzaer/Ultra-Light-Fast-Generic-Face-Detector-1MB) |

---

**人脸识别模型(FaceRecognition)**


| 模型名称                          | 引擎                  | 模型简介                                                                                 | 模型开源网站                                                                                 |
|-------------------------------|---------------------|--------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| InsightFace_IR-SE50(高精度)      | PyTorch             | 这是对 ArcFace（论文）和 InsightFace（GitHub）的 PyTorch 重新实现                                   | [Github](https://github.com/TreB1eN/InsightFace_Pytorch)  |
| InsightFace_Mobilefacenet(极速) | PyTorch             | （轻量级）这是对 ArcFace（论文）和 InsightFace（GitHub）的 PyTorch 重新实现                              | [Github](https://github.com/TreB1eN/InsightFace_Pytorch)  |
| FaceNet                       | PyTorch             | 基于 PyTorch 的 Inception ResNet（V1）模型仓库                                                | [Github](https://github.com/timesler/facenet-pytorch)  |
| ElasticFace                   | PyTorch             | 基于 CVPRW2022 论文《ElasticFace: Elastic Margin Loss for Deep Face Recognition》实现的人脸识别模型 | [Github](https://github.com/fdbtrs/ElasticFace) |
| SeetaFace6              | C++                 | 中科视拓最新开放的开源免费的全栈人脸识别工具包           | [Github](https://github.com/seetafaceengine/SeetaFace6)     |
| SphereFace-20A                | PyTorch+OnnxRuntime | SphereFace:Deep Hypersphere Embedding for Face Recognition        | [Github](https://github.com/wy1iu/sphereface)     |
| DREAM_IJBA_RES18              | OnnxRuntime | DREAM block for Pose-Robust Face Recognition                           | [Github](https://github.com/penincillin/DREAM)     |
| face.evoLVe                   | PyTorch | High-Performance Face Recognition Library based on PaddlePaddle & PyTorch                 | [Github](https://github.com/ZhaoJ9014/face.evoLVe)     |
| VGGFace2                      | PyTorch | PyTorch Face Recognizer based on 'VGGFace2: A dataset for recognising faces across pose and age'.      | [Github](https://github.com/cydonia999/VGGFace2-pytorch)     |


**静态活体检测(RGB)模型（Silent face-anti-spoofing、FaceLivenessDetection）**

| 模型名称      | 引擎             | 模型简介                    | 模型开源网站                                                               |
|-----------|----------------|-------------------------|----------------------------------------------------------------------|
| MiniVision| OnnxRuntime    | 小视科技的静默活体检测             | [Github](https://github.com/minivision-ai/Silent-Face-Anti-Spoofing) |
| IIC_FL(cv_manual_face-liveness_flrgb) | OnnxRuntime | 阿里通义工作室人脸活体检测模型-RGB   | [魔塔](https://www.modelscope.cn/models/iic/cv_manual_face-liveness_flrgb/feedback)                 |
| SeetaFace6 | C++            | 中科视拓最新开放的开源免费的全栈人脸识别工具包 | [Github](https://github.com/seetafaceengine/SeetaFace6)              |


**人脸表情识别模型(FacialExpressionRecognition、fer)**

支持功能：
- 支持识别7种表情：neutral（中性）、happy（高兴）、sad（悲伤）、surprise（惊讶）、fear（恐惧）、disgust（厌恶）、anger（愤怒）

| 模型名称     | 引擎      | 模型简介                     | 模型开源网站                                                               |
|---------|-----------|--------------------------|----------------------------------------------------------------------|
| DensNet121 | PyTorch| FaceLib的densnet121表情识别模型 | [Github](https://github.com/sajjjadayobi/FaceLib/) |
| FrEmotion| OnnxRuntime    | FaceRecognition-LivenessDetection-Javascript      | [Github](https://github.com/Faceplugin-ltd/FaceRecognition-LivenessDetection-Javascript)                 |


**人脸属性识别模型(GenderDetection、AgeDetection、EyeClosenessDetection、FacePoseEstimation)**

支持功能：
- 性别检测
- 年龄检测
- 闭眼检测
- 人脸姿态检测
- 戴口罩检测

| 模型名称       | 模型简介                     | 模型开源网站                                                               |
|------------|--------------------------|----------------------------------------------------------------------|
| SeetaFace6 | 中科视拓最新开放的开源免费的全栈人脸识别工具包 | [Github](https://github.com/seetafaceengine/SeetaFace6)              |


**人脸质量评估模型(FaceQualityAssessment)**

支持功能：
- 亮度评估
- 清晰度评估
- 完整度评估
- 姿态评估
- 遮挡评估

| 模型名称       | 模型简介                     | 模型开源网站                                                               |
|------------|--------------------------|----------------------------------------------------------------------|
| SeetaFace6 | 中科视拓最新开放的开源免费的全栈人脸识别工具包 | [Github](https://github.com/seetafaceengine/SeetaFace6)              |


---

#### 图像分类模型(CLS)

**YOLO 系列**

| 模型名称        | 引擎             | 模型简介       | 模型开源网站                                                               |
|-------------|----------------|------------|----------------------------------------------------------------------|
| YOLOV11-cls | OnnxRuntime | 最流行的图像分类模型 | [Github](https://docs.ultralytics.com/zh/models/)                 |
| YOLOV8-cls      | OnnxRuntime            | 最流行的图像分类模型 | [Github](https://docs.ultralytics.com/zh/models/)              |


#### 目标检测模型

**YOLO 系列**

| 模型名称    | 引擎             | 模型简介                    | 模型开源网站                                                               |
|---------|----------------|-------------------------|----------------------------------------------------------------------|
| YOLOV12 | OnnxRuntime    | 最流行的目标检测模型              | [Github](https://docs.ultralytics.com/zh/models/) |
| YOLOV11 | OnnxRuntime | 最流行的目标检测模型     | [Github](https://docs.ultralytics.com/zh/models/)                 |
| YOLOV8  | OnnxRuntime            | 最流行的目标检测模型 | [Github](https://docs.ultralytics.com/zh/models/)              |

**Tensorflow系列**

仅测试了以下Tensorflow目标检测模型

| 模型名称    | 引擎          | 模型简介           | 模型开源网站                                                               |
|---------|-------------|----------------|----------------------------------------------------------------------|
| EfficientDet | Tensorflow  | Tensorflow目标检测 | [Github](https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/tf2_detection_zoo.md) |
| SSD MobileNet V2 | Tensorflow | Tensorflow目标检测     | [Github](https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/tf2_detection_zoo.md)                 |
| Faster RCNN Inception Resnet V2  | Tensorflow | Tensorflow目标检测     | [Github](https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/tf2_detection_zoo.md)              |

**SSD 系列**

| 模型名称 | 引擎               | 骨干网络 | 输入尺寸 | <div style="width: 60pt">训练数据集</div> | 精度（mAP） | <div style="width: 50pt">推理速度</div> | <div style="width: 150pt">适用场景</div>|
| :-------- |------------------| ------------- | ----------- |--------------------------------------| -------------|-------------------------------------| -------------|
|SSD_300_RESNET5| PyTorch          | ResNet‑50 | 300×300 | COCO                                 | 中等 | 快                                   | 精度需求一般|
|SSD_512_RESNET50_V1_VOC| PyTorch | ResNet‑50 | 512×512 | Pascal VOC                           | 稍高 | 中等                                  | 精度优先、可接受略低速度的场景|
|SSD_512_VGG16_ATROUS_COCO| MXNet | VGG‑16 | 512×512 | COCO                                 | 较高 | 中等                                  | 通用场景；对小目标有一定提升|
|SSD_300_VGG16_ATROUS_VOC| MXNet | VGG‑16 | 300×300 | Pascal VOC                           | 中等偏上 | 快                                   | VOC 数据集同类任务；资源受限时使用|
|SSD_512_MOBILENET1_VOC| MXNet | MobileNet‑1.0 | 512×512 | Pascal VOC                           | 中等 | 快                                   | 嵌入式/移动端设备；算力和内存都很有限|

---

#### 语义分割模型


| 模型名称    | 引擎      | 模型简介                    | 模型开源网站 |
|---------|---------|-------------------------|--------|
| DeepLabv3 | PyTorch | DeepLabv3 是一种图像分割模型，可以把图片里的每个像素分类出来              | 无      |
---

#### 实例分割模型


| 模型名称        | 引擎      | 模型简介                               | 模型开源网站     |
|-------------|---------|------------------------------------|------------|
| YOLOV8-SEG  | OnnxRuntime | Ultralytics在COCO 数据集 上训练的模型        | [Github](https://docs.ultralytics.com/zh/tasks/segment/) |
| YOLOV11-SEG | OnnxRuntime | Ultralytics在COCO 数据集 上训练的模型 |  [Github](https://docs.ultralytics.com/zh/tasks/segment/)        |
| Mask R-CNN | MXNet | Mask R-CNN 是一种在目标检测基础上，同时为每个物体生成像素级分割区域的深度学习模型 | 无          |


#### CLIP模型(Connecting text and images)


| 模型名称        | 引擎          | 模型简介               | 模型开源网站     |
|-------------|-------------|--------------------|------------|
| clip-vit-base-patch32  | PyTorch     | openai的图片及文本特征提取模型 | [Github](https://github.com/openai/CLIP) |

---

#### OBB旋转框目标检测模型

| 模型名称        | 引擎      | 模型简介                           | 模型开源网站     |
|-------------|---------|--------------------------------|------------|
| YOLOV11-OBB | OnnxRuntime | Ultralytics在DOTAv1 数据集 上训练的模型、通过引入一个额外的角度来更准确地定位图像中的对象 | [Github](https://docs.ultralytics.com/zh/tasks/segment/) |
---

#### 零样本目标检测模型

| 模型名称        | 引擎      | 模型简介                           | 模型开源网站                                                   |
|-------------|---------|--------------------------------|----------------------------------------------------------|
| YOLOv8s-worldv2 | PyTorch | 可根据描述性文本检测图像中的任何物体 | [官网](https://docs.ultralytics.com/zh/models/yolo-world/) |
| owlv2-base-patch16 | PyTorch | OWLv2是一种多模态模型，通过结合CLIP的骨干和ViT样的Transformer，实现零样本文本对象检测| [官网](https://huggingface.co/google/owlv2-base-patch16) |
---

#### 行人检测模型

| 模型名称        | 引擎      | 模型开源网站     |
|-------------|---------|-----|
| YOLOV8_PERSON | OnnxRuntime | [Github](https://github.com/YapaLab/yolo-face) |

---

#### 人类动作识别模型

| 模型名称        | 引擎      | 
|-------------|---------|
| VIT_BASE_PATCH16 | PyTorch | 
| INCEPTIONV3_KINETICS400 | OnnxRuntime | 
| INCEPTIONV1_KINETICS400 | OnnxRuntime | 
| RESNET_V1B_KINETICS400 | OnnxRuntime | 
---

#### 姿态估计模型


| 模型名称         | 引擎          | 模型开源网站                                                   |
|--------------|-------------|----------------------------------------------------------|
| YOLO11N-POSE | OnnxRuntime | [Github](https://docs.ultralytics.com/zh/tasks/pose/) |
| YOLO8N-POSE  | OnnxRuntime | [Github](https://docs.ultralytics.com/zh/tasks/pose/) |
| SIMPLE_POSE | MXNet       | 无                 |
---

#### OCR 模型

支持功能：
- 支持简体中文、繁体中文、英文、日文四种主要语言
- 手写、竖版、拼音、生僻字
- 方向矫正

**文本检测模型**

| 模型名称                | 模型简介 | 模型开源网站                                                                                                       |
| ------------| ------------------- |--------------------------------------------------------------------------------------------------------------|
| PP-OCRv5_server_det | 服务端文本检测模型，精度更高，适合在性能较好的服务器上部署         | [Github](https://github.com/PaddlePaddle/PaddleOCR/blob/main/docs/version3.x/algorithm/PP-OCRv5/PP-OCRv5.md) |
| PP-OCRv5_mobile_det | 轻量文本检测模型，效率更高，适合在端侧设备部署         | [Github](https://github.com/PaddlePaddle/PaddleOCR/blob/main/docs/version3.x/algorithm/PP-OCRv5/PP-OCRv5.md) |
| PP-OCRv4_server_det | 服务端文本检测模型，精度更高，适合在性能较好的服务器上部署         | [Github](https://github.com/PaddlePaddle/PaddleOCR/blob/main/docs/version3.x/algorithm/PP-OCRv5/PP-OCRv5.md) |
| PP-OCRv4_mobile_det | 轻量文本检测模型，效率更高，适合在端侧设备部署         | [Github](https://github.com/PaddlePaddle/PaddleOCR/blob/main/docs/version3.x/algorithm/PP-OCRv5/PP-OCRv5.md) |

**文本识别模型**

| 模型名称                | 模型简介                                                                                                                 | 模型开源网站                                                                                                       |
| ------------|----------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| PP-OCRv5_server_rec | （服务端）致力于以单一模型高效、精准地支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字等复杂文本场景的识别。在保持识别效果的同时，兼顾推理速度和模型鲁棒性，为各种场景下的文档理解提供高效、精准的技术支撑。 | [Github](https://github.com/PaddlePaddle/PaddleOCR/blob/main/docs/version3.x/algorithm/PP-OCRv5/PP-OCRv5.md) |
| PP-OCRv5_mobile_rec | （轻量）致力于以单一模型高效、精准地支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字等复杂文本场景的识别。在保持识别效果的同时，兼顾推理速度和模型鲁棒性，为各种场景下的文档理解提供高效、精准的技术支撑。  | [Github](https://github.com/PaddlePaddle/PaddleOCR/blob/main/docs/version3.x/algorithm/PP-OCRv5/PP-OCRv5.md) |
| PP-OCRv4_server_rec | （服务端）推理精度高，可以部署在多种不同的服务器上                                                                                                    | [Github](https://github.com/PaddlePaddle/PaddleOCR/blob/main/docs/version3.x/algorithm/PP-OCRv5/PP-OCRv5.md) |
| PP-OCRv4_mobile_rec | （轻量） 效率更高，适合在端侧设备部署                                                                                                  | [Github](https://github.com/PaddlePaddle/PaddleOCR/blob/main/docs/version3.x/algorithm/PP-OCRv5/PP-OCRv5.md) |


**文本方向分类模型(cls)**

| 模型名称                | 模型简介                         | 模型开源网站     |
| ------------|------------------------------|------------|
| ch_ppocr_mobile_v2.0_cls   | 原始分类器模型，对检测到的文本行文字角度分类       | [Github](https://paddlepaddle.github.io/PaddleOCR/latest/en/version2.x/ppocr/model_list.html?h=models+list#13-multilingual-detection-model) |
| PP_LCNET_X0_25   | （轻量）基于PP-LCNet_x0_25的文本行分类模型 | [Github](https://paddlepaddle.github.io/PaddleOCR/v3.1.0/version3.x/module_usage/textline_orientation_classification.html) |
| PP_LCNET_X1_0   | 基于PP-LCNet_x1_0的文本行分类模型      | [Github](https://paddlepaddle.github.io/PaddleOCR/v3.1.0/version3.x/module_usage/textline_orientation_classification.html) |


**表格结构识别(Table Structure Recognition)**

| 模型名称                | 模型简介                         | 模型开源网站     |
| ------------|------------------------------|------------|
| SLANet   | 该模型通过轻量级骨干 PP-LCNet、CSP-PAN 融合与 SLA Head 解码，有效提升表格结构识别的精度与速度。       | [Github](https://paddlepaddle.github.io/PaddleOCR/v3.1.0/version3.x/module_usage/table_structure_recognition.html#_3) |
| SLANet_plus   | （增强版）该模型通过轻量级骨干 PP-LCNet、CSP-PAN 融合与 SLA Head 解码，有效提升表格结构识别的精度与速度。 | [Github](https://paddlepaddle.github.io/PaddleOCR/v3.1.0/version3.x/module_usage/table_structure_recognition.html#_3) |


**车牌检测模型(License Plate Detection)**

| 模型名称    | 模型简介                     | 模型开源网站     |
|---------|--------------------------|------------|
| YOLOV5  | 基于YOLOV5训练，支持12种中文车牌     | [Github](https://github.com/we0091234/Chinese_license_plate_detection_recognition) |
| yolov7-lite-t | （超小型模型）YOLOv7-Lite 架构的轻量级车牌检测模型 | [Github](https://github.com/we0091234/Chinese_license_plate_detection_recognition) |
| yolov7-lite-s  | YOLOv7-Lite 架构的轻量级车牌检测模型 | [Github](https://github.com/we0091234/Chinese_license_plate_detection_recognition) |


**车牌识别模型(License Plate Recognition)**

| 模型名称   | 模型简介       | 模型开源网站     |
|--------|------------|------------|
| PLATE_REC_CRNN | CRNN中文字符识别 | [Github](https://github.com/Sierkinhane/CRNN_Chinese_Characters_Rec) |


---

#### 机器翻译模型

支持功能
- 200多语言互相翻译

| 模型名称          | 模型简介                                    | 模型官网                                          |
|---------------|-----------------------------------------|-----------------------------------------------|
| NLLB-200      | Meta AI 开发的一个先进的单一多语言机器翻译模型             | [Github](https://github.com/facebookresearch/fairseq/tree/nllb) |
| OPUS_MT_ZH_EN | Helsinki-NLP团队开发的中英翻译模型，基于Transformer架构 | [huggingface](https://huggingface.co/Helsinki-NLP/opus-mt-zh-en) |
| OPUS_MT_EN_ZH | Helsinki-NLP团队开发的英中翻译模型，基于Transformer架构 | [huggingface](https://huggingface.co/Helsinki-NLP/opus-mt-en-zh) |

---

#### 语音识别模型(ASR)

这里仅介绍模型的开源项目，每个开源项目通常包含多个具体模型，本文不逐一列出。

| 模型名称    | 模型简介                                                                    | 模型官网                                          |
|---------|-------------------------------------------------------------------------|-----------------------------------------------|
| Whisper | OpenAI 开源的通用语音识别（ASR）模型，支持多语言转写和翻译，具有较高的识别精度，尤其在嘈杂环境中表现良好，适合离线和批量音频处理。  | [Github](https://github.com/ggml-org/whisper.cpp) |
| Vosk    | 一个轻量级离线语音识别工具包，支持多种语言和平台（包括移动端与嵌入式设备），可在低资源环境中运行，适合实时语音识别场景。            | [Github](https://github.com/alphacep/vosk-api) |
| sherpa-onnx    | Sherpa-ONNX 是一个基于 ONNX Runtime 的ASR 及 TTS 推理框架 | [Github](https://github.com/k2-fsa/sherpa-onnx) |

#### 语音合成模型(TTS)

这里仅介绍模型的开源项目，每个开源项目通常包含多个具体模型，本文不逐一列出。

| 模型名称    | 模型简介                                                                    | 模型官网                                          |
|---------|-------------------------------------------------------------------------|-----------------------------------------------|
| sherpa-onnx    | Sherpa-ONNX 是一个基于 ONNX Runtime 的ASR 及 TTS 推理框架 | [Github](https://github.com/k2-fsa/sherpa-onnx) |


---

## 🙏 鸣谢

本项目在开发过程中借鉴或使用了以下优秀开源项目，特此致谢：

- **[Deep Java Library](https://docs.djl.ai)**
- **[AIAS](https://gitee.com/mymagicpower/AIAS)**

## 联系方式

如您在使用过程中有任何问题、建议，或希望进行技术交流与合作，欢迎添加微信与我联系，并加入用户交流群。

- **微信**: deng775747758 （请备注：SmartJavaAI）
- **Email**: 775747758@qq.com


🚀 **如果这个项目对你有帮助，别忘了点个 Star ⭐！你的支持是我持续优化升级的动力！** ❤️

## 献代码的步骤

1、在Gitee或者Github/Gitcode上fork项目到自己的repo

2、把fork过去的项目也就是你的项目clone到你的本地

3、修改代码（记得一定要修改dev分支）

4、commit后push到自己的库（dev分支）

5、登录Gitee或Github/Gitcode在你首页可以看到一个 pull request 按钮，点击它，填写一些说明信息，然后提交即可。

6、等待维护者合并



