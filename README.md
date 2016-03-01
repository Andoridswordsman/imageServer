# 项目简介
图片服务器，能够对源图片进行转换（指定高宽，等比缩放，添加水印，添加业务图标，改变透明度等），并且进行业务图片的缓存，提供业务图片的静态地址等
（目前本项目还是第一版,还有很多不完善的地方,但是功能使用完全没有问题.后续会继续更进更新）

# 配置说明
- system.properties : 配置系统的参数,如FTP的访问配置等
- noimage.jpg : 这个图片是图片服务器在未找到FTP上指定的图片的时候,或者图片处理出现异常时,返回的默认图片
- tuijianBig.png : 偏大尺寸的推荐水印
- tuijianSmall.png : 偏小尺寸的推荐水印
- watermark.png : 版权水印

# 项目使用说明
1. 需要配置一个FTP服务器,用于存放源图片.imageServer将会根据system.properties中配置的FTP信息进行访问
2. 如目前有一个FTP服务器上的图片:/image/user/demo.jpg :
	- 获取200 * 200 像素的业务图片的静态地址 : 
	http://webRoot:8080/getImagePath?path=/image/user/demo.jpg&width=200&height=200
	- 直接获取显示200 * 200 像素的业务图片 : 
	http://webRoot:8080/image?path=/image/user/demo.jpg&width=200&height=200
	- 具体的param参数的说明请查看Controller的方法注释
