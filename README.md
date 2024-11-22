# CustomVideoPlayer

在这里记录下开发进度



## 2024.11.22

创建了项目，引入了ExoPlayer2到项目中作为播放器的插件，初步考虑了大概框架和实现思路，

播放功能放到Service中，以实现后台播放的功能，然后Activity通过bindService的方式和Service交互，从而控制播放进度。

当前问题：

1.传入视频Path会提示权限问题：

```
Permission to access file:/storage/emulated/0/Download/QuarkDownloads/CloudDrive/c6d30dc45ad030fba581e89a33740ed3/321123.mov is denied uid = 10405 forWrite = false

java.lang.SecurityException: com.org.customvideoplayer has no access to content://media/external_primary/file/1000053355 
	forWrite = false
		at com.android.providers.media.MediaProvider.enforceCallingPermissionInternal(MediaProvider.java:10568)
		at com.android.providers.media.MediaProvider.enforceCallingPermission(MediaProvider.java:10465)
		at com.android.providers.media.MediaProvider.checkAccess(MediaProvider.java:10592)
		at com.android.providers.media.MediaProvider.checkIfFileOpenIsPermitted(MediaProvider.java:9586)
		at com.android.providers.media.MediaProvider.onFileOpenForFuse(MediaProvider.java:9699)
```

