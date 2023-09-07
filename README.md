Android SDK 线上文档地址：https://ai.baidu.com/ai-doc/EASYDL/ak38n3x70

快速开始：
1. 打开AndroidStudio， 点击 "Import Project..."。在一台较新的手机上测试。

EasyDL及EasyEdge的模型，需要序列号。开源模型不需要。
1. MainActivity.java 修改序列号
2. app/build.gradle文件，修改"com.baidu.ai.easyaimobile.demo"为申请的包名


对于通用ARM的常见功能，项目内自带精简版，可以忽略开发板不兼容的摄像头。
此外，由于实时摄像开启，会导致接口的耗时变大，此时也可以使用精简版测试。
修改启动Activity为infertest/MainActivity 测试精简版