# 应用配置

一个类似「应用变量」的可在宿主应用内完成配置的适用于「LSPatch 便携模式」的模块

## 特色

- 支持修改应用内 DPI（配置时使用「最小宽度」）
- 支持在不允许获取「读取应用列表」权限的设备上模拟「读取应用列表」权限
- 支持为应用内界面增加边距以实现圆屏适配
- 可以按设备和应用生效的自定义配置
  - 以规则集为单位
  - 设备与应用包名双条件约束
  - 可以自定义最小宽度大小
  - 可以自定义模拟权限情况
  - 可以自定义圆屏适配时界面容器的长宽比例
  - 可以自定义圆屏适配时界面的缩放系数

## 配置文件说明

自定义配置不提供 GUI 配置界面，请自行使用「MT 管理器」等工具修改模块 apk 内的 [`assets/config.xml`](https://github.com/jiwangyihao/app_config/blob/main/app/src/main/assets/config.xml) 文件进行配置。

**欢迎提交 Pull Request 来让我们的默认配置更好！**

_即使你没有能力更改配置文件，也可以通过 Issue 来帮助我们！_

示例文件如下（你也可以点击 [`assets/example.xml`](https://github.com/jiwangyihao/app_config/blob/main/app/src/main/assets/config.xml) 查看当前最新版本）：
``` xml
<?xml version="1.0" encoding="utf-8" ?>
<!-- 这是一份示例配置，对它的修改 **不会** 起效 -->

<!-- <config> 是整个配置文件的根元素 -->
<!-- 每个配置文件中只能有一个 <config> -->
<!-- author 属性用于标记当前配置的作者 -->
<!-- 既然你已经看到了这段注释，那么，作为修改配置的开始 -->
<!-- 把它改成你的 ID 吧！ -->
<config author="吉王义昊">
    <!-- <rule-set> 标签标记一组配置，device 属性用于标记需生效的设备代码 -->
    <!-- 机型代码可使用「设备信息」获取（「设备」选项卡下第一个卡片的「设备」项） -->
    <!-- 当不存在 device 属性时，该组配置对所有设备生效 -->
    <!-- package 属性用于标记需生效的宿主应用包名 -->
    <!-- 宿主应用包名可使用 MT 管理器获取 -->
    <!-- 当不存在 package 属性时，该组配置对所有宿主应用生效 -->
    <rule-set>
        <!-- 最小宽度设置（整数型，默认值是 320） -->
        <!-- 该项设置与开发者选项中的最小宽度效果基本相同 -->
        <!-- （部分系统，比如 MIUI 的开发者选项内的最小宽度设置存在问题，可能效果会不同） -->
        <!-- 最小宽度表示屏幕在宽上以 dp 表示的长度 -->
        <!-- 最小宽度越大，屏幕能容纳的独立像素（dp）就越多，能显示的内容就越多 -->
        <!-- 相应的界面元素就越小 -->
        <!-- 最小宽度与 dpi 之间的换算关系为： -->
        <!-- 最小宽度 * dpi / 160 = 屏幕短边物理像素数 -->
        <!-- 注意：尽管使用本模块无需考虑对系统的影响，但过大或者过小的数值可能会不生效 -->
        <!-- 注意：当开启圆屏适配后，会忽略被裁切的那部分屏幕大小 -->
        <!-- 也就是说，新的换算关系为： -->
        <!-- 最小宽度 * dpi / 160 = 容器宽上的物理像素数 -->
        <item name="minWidth">320</item>
        <!-- 模拟应用列表权限（布尔型，默认值是 true） -->
        <item name="fakeAppList">true</item>
        <!-- 开启圆屏适配（布尔型，默认值是 true） -->
        <!-- 注意：只有当屏幕高度与宽度之比在 0.9~1.1 时该配置才会生效 -->
        <item name="round">true</item>
        <!-- 强制开启圆屏适配（布尔型，默认值是 false） -->
        <!-- 即使屏幕高度与宽度之比在 0.9~1.1 外也开启圆屏适配 -->
        <item name="forceRound">false</item>
        <!-- 圆屏适配下界面容器的大小（浮点型，默认值是 1.0） -->
        <!-- 当该项设置被设置为 1.0 时，界面容器会被缩小为全屏的 1/√2 -->
        <!-- 经过简单的数学推导即可得到这就是一个圆中最大正方形的大小 -->
        <!-- 当该项设置被修改后，界面容器会在 1.0 缩放的基础上乘该值 -->
        <!-- 也就是说，当设置为 1.1 时，界面容器会在 1.0 的基础上增大 10% -->
        <!-- 实际上该项设置的是界面容器对角线的长度，默认为圆形屏幕的直径 -->
        <item name="roundSize">1.0</item>
        <!-- 圆屏适配下界面容器的比例（浮点型，默认值是 1.0） -->
        <!-- 容器高度除以宽度所得的结果 -->
        <!-- 当该项设置被设置为 1.0 时，界面容器是一个标准正方形 -->
        <item name="roundRatio">1.5</item>
    </rule-set>
    <!-- 单独配置，当在机型代码是 round-device 的设备上运行时，该配置生效 -->
    <rule-set device="round-device">
        <!-- 单独配置可以只配置部分项目 -->
        <item name="minWidth">268</item>
        <!-- 为某个圆形屏幕的设备开启圆屏适配 -->
        <item name="round">true</item>
    </rule-set>
    <!-- 单独配置，当在包名是 com.example.app 的宿主应用上运行时，该配置生效 -->
    <rule-set package="com.example.app">
        <!-- 单独配置可以只配置部分项目 -->
        <item name="minWidth">268</item>
    </rule-set>
    <!-- 设备与应用的限制条件可以组合，当在机型代码是 example-device 并且包名是 com.example.app 的宿主应用上运行时，该配置生效 -->
    <rule-set package="com.example.app">
        <!-- 单独配置可以只配置部分项目 -->
        <item name="minWidth">268</item>
    </rule-set>
</config>

<!-- 所有符合条件的配置都会被应用（通用配置或者符合限制条件的配置） -->
<!-- 写在后面的配置项优先级更高，后面的配置项会覆盖前面的已有配置项 -->
<!-- 值得注意的是，单独配置并不享有额外的优先级（与 CSS 不同） -->
<!-- 也就是说，如果你把通用配置写在最后，所有的单独配置都无法生效 -->

<!-- 警告：尽管编写模块时做了容错处理，但错误的配置仍然可能使模块部分甚至整体失效 -->
```

## 鸣谢

本应用主要基于（或者参考）了以下开源项目：
- https://github.com/fankes/YukiHookAPI
- https://github.com/square/okio
- https://github.com/BlueCat300/XposedAppSettings

（详见依赖）

## Copyright

© copyright Lordly Team

## ✨ Stargazers over time

感谢大家的支持！

[![Stargazers over time](https://starchart.cc/jiwangyihao/app_config.svg?variant=adaptive)](https://starchart.cc/jiwangyihao/app_config)
