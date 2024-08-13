package top.jwyihao.appconfig.hook

import android.app.Activity
import android.app.Application.getProcessName
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.ActivityClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import okio.buffer
import okio.source
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream
import java.io.StringReader
import java.util.zip.ZipInputStream
import kotlin.math.*

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        // Your code here.
        debugLog {
            isRecord = false
            tag = "AppConfigModule"
        }
    }

    override fun onHook() = encase {
        // Your code here.
        val config = Config()

        try {
            val regex = """(?<=/)[\w.]+\.[\w.]+(?=/cache/lspatch)""".toRegex()
            YLog.debug(msg = "包名：${regex.find(moduleAppFilePath)?.value}", tag = YLog.Configs.tag)
            val appPackage = regex.find(moduleAppFilePath)?.value

            val file = File(moduleAppFilePath)
            val inputStream = FileInputStream(file)
            val zipInputStream = ZipInputStream(inputStream)
            var zipEntry = zipInputStream.nextEntry
            while (zipEntry != null) {
                val name = zipEntry.name
                if (name == "assets/config.xml") {
                    val size = zipEntry.size.toInt()
                    val buffer = ByteArray(size)
                    var count = 0
                    while (count < size) {
                        val n = zipInputStream.read(buffer, count, size - count)
                        if (n == -1) {
                            break
                        }
                        count += n
                    }
                    val xmlString = String(buffer)

                    val parser = XmlPullParserFactory.newInstance().newPullParser()
                    //设置输入流的编码
                    //设置输入流的编码
                    parser.setInput(StringReader(xmlString))
                    //得到第一个事件类型
                    //得到第一个事件类型
                    var eventType = parser.eventType
                    var isSuit: Boolean? = null
                    //如果事件类型不是文档结束的话则不断处理事件
                    //如果事件类型不是文档结束的话则不断处理事件
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {
                            XmlPullParser.START_DOCUMENT -> YLog.debug(
                                msg = "开始解析配置文件",
                                tag = YLog.Configs.tag
                            )

                            XmlPullParser.START_TAG -> {
                                //获得解析器当前元素的名称
                                val tagName = parser.name
                                //如果当前标签名称是 <config>
                                if ("config" == tagName) {
                                    //修改配置文件作者一项
                                    config.author = parser.getAttributeValue(null, "author")
                                }
                                //如果当前标签名称是 <rule-set>
                                if ("rule-set" == tagName) {
                                    //标记 device name
                                    val device = parser.getAttributeValue(null, "device")
                                    val app = parser.getAttributeValue(null, "package")
                                    isSuit =
                                        ((device == null) or (device == Build.DEVICE)) and ((app == null) or (app == appPackage))
                                }
                                //如果当前规则集符合条件
                                if (isSuit == true) {
                                    when (parser.getAttributeValue(null, "name")) {
                                        "minWidth" -> config.minWidth = parser.nextText().toInt()

                                        "fakeAppList" -> config.fakeAppList =
                                            parser.nextText().toBoolean()

                                        "round" -> config.round = parser.nextText().toBoolean()

                                        "forceRound" -> config.forceRound =
                                            parser.nextText().toBoolean()

                                        "roundSize" -> config.roundSize =
                                            parser.nextText().toDouble()

                                        "roundRatio" -> config.roundRatio =
                                            parser.nextText().toDouble()

                                        "horizontalOffset" -> config.horizontalOffset =
                                            parser.nextText().toDouble()

                                        "verticalOffset" -> config.verticalOffset =
                                            parser.nextText().toDouble()

                                        "backgroundColor" -> config.backgroundColor =
                                            parser.nextText()

                                        "backgroundAlpha" -> config.backgroundAlpha =
                                            parser.nextText().toInt()
                                    }
                                }
                            }

                            XmlPullParser.END_TAG ->                     //如果是 rule-set 标签结束
                                if ("rule-set" == parser.name) {
                                    //置空
                                    isSuit = null
                                }
                        }
                        //进入下一个事件处理
                        eventType = parser.next()
                    }
                }
                zipEntry = zipInputStream.nextEntry
            }
            zipInputStream.close()
            inputStream.close()

            YLog.debug(msg = "minWidth：${config.minWidth}", tag = YLog.Configs.tag)
            YLog.debug(msg = "fakeAppList：${config.fakeAppList}", tag = YLog.Configs.tag)
            YLog.debug(msg = "round：${config.round}", tag = YLog.Configs.tag)
            YLog.debug(msg = "forceRound：${config.forceRound}", tag = YLog.Configs.tag)
            YLog.debug(msg = "roundSize：${config.roundSize}", tag = YLog.Configs.tag)
            YLog.debug(msg = "roundRatio：${config.roundRatio}", tag = YLog.Configs.tag)
            YLog.debug(msg = "horizontalOffset：${config.horizontalOffset}", tag = YLog.Configs.tag)
            YLog.debug(msg = "verticalOffset：${config.verticalOffset}", tag = YLog.Configs.tag)
        } catch (e: Exception) {
            YLog.error(msg = "获取配置文件失败", e = e, tag = YLog.Configs.tag)
        }

        loadZygote {
            /*
            "android.view.Display".toClass()
                .method {
                    name = "updateDisplayInfoLocked"
                    emptyParam()
                }
                .hook {
                    after {
                        //Toast.makeText(appContext, "DPI Hooking", Toast.LENGTH_SHORT).show();

                        // Density for this package is overridden, change density
                        YLog.debug(msg = "updateDisplayInfoLocked hook", tag = YLog.Configs.tag)
                        instanceClass?.field { name = "mDisplayInfo" }?.get(instance)?.current()
                            ?.field { name = "logicalDensityDpi" }
                            ?.set(systemContext.resources.displayMetrics.widthPixels * 160 / config.minWidth)
                        instanceClass?.field { name = "mDisplayInfo" }?.get(instance)?.current()
                            ?.field { name = "logicalWidth" }
                            ?.set(systemContext.resources.displayMetrics.widthPixels - 100)
                    }
                }
             */

            if (config.fakeAppList) {
                "android.app.ApplicationPackageManager".toClass().method {
                    name = "getInstalledPackages"
                    param(IntType)
                }.hook {
                    after {
                        YLog.debug(msg = "getInstalledPackages hook", tag = YLog.Configs.tag)
                        try {
                            val pm: PackageManager = systemContext.packageManager
                            val pmList = mutableListOf<String>()
                            val process = Runtime.getRuntime().exec("pm list packages")
                            process.inputStream.source().buffer().use { bs ->
                                while (true) {
                                    bs.readUtf8Line()?.trim()?.let { line ->
                                        if (line.startsWith("package:")) {
                                            line.removePrefix("package:")
                                                .takeIf { removedPrefix -> removedPrefix.isNotBlank() }
                                                ?.let { pmList.add(it) }
                                        }
                                    } ?: break
                                }
                            }
                            val appList = pmList.asSequence().map {
                                pm.getPackageInfo(
                                    it,
                                    PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS
                                )
                            }.filter { it.applicationInfo.sourceDir != null }.toList()
                            result = appList
                        } catch (t: Throwable) {
                            YLog.error(msg = "[应用列表]", e = t, tag = YLog.Configs.tag)
                        }
                    }
                }
            }
        }

        loadApp {
            onAppLifecycle {
                onCreate {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            if (mainProcessName == getProcessName()) {
                                Toast.makeText(
                                    appContext,
                                    "「应用配置」运行中\r\n该配置由「${config.author}」提供",
                                    Toast.LENGTH_SHORT
                                ).show()
                                YLog.debug(
                                    msg = "「应用配置」运行中，当前配置由「${config.author}」提供",
                                    tag = YLog.Configs.tag
                                )
                                YLog.debug(
                                    msg = "PID：「${getProcessName()}」",
                                    tag = YLog.Configs.tag
                                )
                            }
                        }

                        YLog.saveToFile("${Environment.getExternalStorageDirectory().path}Android/data/$packageName/appconfig.log")
                        YLog.clear()
                    } catch (e: Exception) {
                        YLog.error(msg = "Base Hook Failed", e = e, tag = YLog.Configs.tag)
                    }
                }
            }

            "android.content.ContextWrapper".toClass().method {
                name = "attachBaseContext"
                paramCount = 1
            }.hook {
                before {
                    try {
                        YLog.debug(msg = "ContextWrapper hook", tag = YLog.Configs.tag)
                        YLog.saveToFile("${Environment.getExternalStorageDirectory().path}Android/data/$packageName/appconfig.log")
                        YLog.clear()
                        var context = args().first().cast<Context?>()
                        val res: Resources = context!!.resources
                        val configure = Configuration(res.configuration)
                        val runningMetrics = res.displayMetrics

                        /*
                        val newMetrics: DisplayMetrics?
                        if (runningMetrics != null) {
                            newMetrics = DisplayMetrics()
                            newMetrics.setTo(runningMetrics)
                        } else {
                            newMetrics = res?.displayMetrics
                        }
                         */

                        var minWidth = config.minWidth

                        if (
                            (config.round and (abs((1f * runningMetrics.heightPixels / runningMetrics.widthPixels) - 1) < 0.1))
                            or config.forceRound
                        ) {
                            val containerDiagonal =
                                sqrt(runningMetrics.widthPixels * runningMetrics.heightPixels * 1f) * config.roundSize
                            val containerWidth = containerDiagonal * cos(atan(config.roundRatio))
                            minWidth =
                                (minWidth / containerWidth * runningMetrics.widthPixels).toInt()
                        }

                        configure.current().field { name = "densityDpi" }
                            .set(runningMetrics.widthPixels * 160 / minWidth)

                        /*
                        newMetrics?.density = runningMetrics?.widthPixels?.div(config.minWidth.toFloat())
                        newMetrics?.densityDpi = runningMetrics?.widthPixels?.times(160)
                            ?.div(config.minWidth)
                        newMetrics?.widthPixels = runningMetrics?.widthPixels?.minus(100)
                        configure.smallestScreenWidthDp -= 100
                        configure.screenWidthDp -= 100
                         */

                        context = context.createConfigurationContext(configure)

                        /*
                        context?.resources?.current()?.field { name = "mResourcesImpl" }?.any()
                            ?.current()?.field { name = "mMetrics" }?.set(newMetrics)
                        val windowManager = context?.getSystemService(WINDOW_SERVICE) as WindowManager
                        val display = windowManager.defaultDisplay
                        display?.current()?.field { name = "mDisplayInfo" }?.any()?.current()
                            ?.field { name = "logicalDensityDpi" }
                            ?.set(systemContext.resources.displayMetrics.widthPixels * 160 / config.minWidth)
                        display?.current()?.field { name = "mDisplayInfo" }?.any()?.current()
                            ?.field { name = "logicalWidth" }
                            ?.set(systemContext.resources.displayMetrics.widthPixels - 100)
                        context = display?.let { context?.createDisplayContext(it) }
                         */

                        args().first().set(context)
                        YLog.debug(msg = "ContextWrapper hook after", tag = YLog.Configs.tag)
                        YLog.saveToFile("${Environment.getExternalStorageDirectory().path}Android/data/$packageName/appconfig.log")
                        YLog.clear()
                    } catch (e: Exception) {
                        YLog.error(msg = "Base Hook Failed", e = e, tag = YLog.Configs.tag)
                    }
                }
            }

            if (config.round) {
                ActivityClass.method {
                    name = "onCreate"
                }.hook {
                    after {
                        val metrics = appContext!!.resources.displayMetrics

                        if (
                            (config.round and (abs((1f * metrics.heightPixels / metrics.widthPixels) - 1) < 0.1))
                            or config.forceRound
                        ) {
                            val rootView = instance<Activity>().findViewById<View>(android.R.id.content)

                            val containerDiagonal =
                                sqrt(metrics.widthPixels * metrics.heightPixels * 1f) * config.roundSize
                            val containerWidth = containerDiagonal * cos(atan(config.roundRatio))
                            val containerHeight = containerDiagonal * sin(atan(config.roundRatio))
                            val horizontalPadding =
                                (metrics.widthPixels - containerWidth.toInt()) / 2
                            val verticalPadding =
                                (metrics.heightPixels - containerHeight.toInt()) / 2
                            val leftPadding = horizontalPadding * (1 + config.horizontalOffset)
                            val topPadding = verticalPadding * (1 + config.verticalOffset)
                            val rightPadding = horizontalPadding * (1 - config.horizontalOffset)
                            val bottomPadding = verticalPadding * (1 - config.verticalOffset)
                            rootView
                                .setPadding(
                                    leftPadding.toInt(),
                                    topPadding.toInt(),
                                    rightPadding.toInt(),
                                    bottomPadding.toInt()
                                )

                            if (config.backgroundColor != "undefined") {
                                instance<Activity>().window.decorView.setBackgroundColor(Color.parseColor(config.backgroundColor))
                                rootView.background.alpha = 0
                            }

                            if (config.backgroundAlpha != -1) {
                                instance<Activity>().window.decorView.background.alpha = config.backgroundAlpha
                                rootView.background.alpha = 0
                            }

                            YLog.debug(msg = "Width：${metrics.widthPixels}", tag = YLog.Configs.tag)
                            YLog.debug(
                                msg = "Height：${metrics.heightPixels}",
                                tag = YLog.Configs.tag
                            )
                            YLog.debug(
                                msg = "containerWidth：$containerWidth",
                                tag = YLog.Configs.tag
                            )
                            YLog.debug(
                                msg = "containerHeight：$containerHeight",
                                tag = YLog.Configs.tag
                            )
                            YLog.debug(
                                msg = "horizontalPadding：$horizontalPadding",
                                tag = YLog.Configs.tag
                            )
                            YLog.debug(
                                msg = "verticalPadding：$verticalPadding",
                                tag = YLog.Configs.tag
                            )
                            YLog.debug(
                                msg = "leftPadding：$leftPadding",
                                tag = YLog.Configs.tag
                            )
                            YLog.debug(
                                msg = "topPadding：$topPadding",
                                tag = YLog.Configs.tag
                            )
                            YLog.debug(
                                msg = "rightPadding：$rightPadding",
                                tag = YLog.Configs.tag
                            )
                            YLog.debug(
                                msg = "bottomPadding：$bottomPadding",
                                tag = YLog.Configs.tag
                            )
                        }
                    }
                }

                "android.app.Dialog".toClass().method {
                    name = "show"
                }.hook {
                    after {
                        val window = instance<Dialog>().window

                        val metrics = appContext!!.resources.displayMetrics

                        if (
                            (config.round and (abs((1f * metrics.heightPixels / metrics.widthPixels) - 1) < 0.1))
                            or config.forceRound
                        ) {
                            val containerDiagonal =
                                sqrt(metrics.widthPixels * metrics.heightPixels * 1f) * config.roundSize
                            val containerWidth = containerDiagonal * cos(atan(config.roundRatio))
                            val containerHeight = containerDiagonal * sin(atan(config.roundRatio))
                            val horizontalPadding =
                                (metrics.widthPixels - containerWidth.toInt()) / 2
                            val verticalPadding =
                                (metrics.heightPixels - containerHeight.toInt()) / 2

                            //设置window背景，默认的背景会有Padding值，不能全屏。当然不一定要是透明，你可以设置其他背景，替换默认的背景即可。
                            //window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            //一定要在setContentView之后调用，否则无效
//                            window?.setLayout(containerWidth.toInt(), containerHeight.toInt())
                            window?.setGravity(Gravity.END or Gravity.BOTTOM)
                            val attributes = window!!.attributes
                            attributes.gravity = Gravity.END or Gravity.BOTTOM
                            attributes.x = horizontalPadding
                            attributes.y = verticalPadding
//                            attributes.width = containerWidth.toInt()
//                            attributes.height = containerHeight.toInt()
                            instance<Dialog>().window?.attributes = attributes
                            val rootView =
                                instance<Dialog>().findViewById<View>(android.R.id.content)
                            val param: FrameLayout.LayoutParams =
                                FrameLayout.LayoutParams(
                                    containerWidth.toInt(),
                                    (containerHeight * 0.8).toInt()
                                )
                            param.gravity = Gravity.CENTER
                            //param.setMargins(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                            //rootView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                            rootView.layoutParams = param
                            YLog.debug(
                                msg = "Dialog Hook ${rootView.layoutParams.height}",
                                tag = YLog.Configs.tag
                            )
                        }
                    }
                }
            }
        }
    }
}

class Config {
    var minWidth = 320
    var fakeAppList = true
    var author = "吉王义昊"
    var round = true
    var forceRound = true
    var roundSize = 1.0
    var roundRatio = 1.0
    var horizontalOffset = 0.0
    var verticalOffset = 0.0
    var backgroundColor = "undefined"
    var backgroundAlpha = -1
}