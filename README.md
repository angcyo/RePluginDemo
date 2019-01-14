# RePluginDemo
Android--›360全面插件化RePlugin框架交互通信使用概述

# 简要说明

`host` 为`宿主`工程, 用来加载插件的
`plugin1` 为'插件1'工程
`plugin2` 为'插件2'工程

>宿主工程没有内置插件
>外置插件请自行编译打包相应的`plugin1`和`plugin2`工程

# 使用前

`host`项目内, 默认使用:

`插件1` 对应`plugin1` 工程.
`插件2` 对应`plugin2` 工程.

## 安装插件

打包`plugin1`和`plugin2`工程.
```
adb push plugin1-release.apk /sdcard/plugin/plugin1.apk
```

```
adb push plugin2-release.apk /sdcard/plugin/plugin2.apk
```
将apk, 推送到 手机sd卡.

之后, 就可以点击按钮安装插件了.
