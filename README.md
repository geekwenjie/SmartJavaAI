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








## 🚀  能力展示

<div align="center">
  <table>
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



