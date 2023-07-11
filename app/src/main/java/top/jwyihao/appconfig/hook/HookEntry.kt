package top.jwyihao.appconfig.hook

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.widget.Toast
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.log.loggerE
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


@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        // Your code here.
        debugLog {
            isRecord = false
        }
    }

    override fun onHook() = encase {
        // Your code here.
        val config=Config()

        try {
            val file =
                File(moduleAppFilePath)
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
                    var device:String?=null
                    //如果事件类型不是文档结束的话则不断处理事件
                    //如果事件类型不是文档结束的话则不断处理事件
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {
                            XmlPullParser.START_DOCUMENT ->
                                loggerD(msg="开始解析配置文件")

                            XmlPullParser.START_TAG -> {
                                //获得解析器当前元素的名称
                                val tagName = parser.name
                                //如果当前标签名称是 <config>
                                if ("config" == tagName) {
                                    //修改配置文件作者一项
                                    config.author=parser.getAttributeValue(null,"author")
                                }
                                //如果当前标签名称是 <device>
                                if ("device" == tagName) {
                                    //标记 device name
                                    device=parser.getAttributeValue(null,"name")
                                }
                                //如果 device 已经读取
                                if (device != null) {
                                    //如果 device name 是默认或者当前设备名
                                    if (device==Build.DEVICE||device=="default") {
                                        when (parser.getAttributeValue(null,"name")) {
                                            "minWidth"->
                                                config.minWidth=parser.nextText().toInt()

                                            "fakeAppList"->
                                                config.fakeAppList=parser.nextText().toBoolean()
                                        }
                                    }
                                }
                            }

                            XmlPullParser.END_TAG ->                     //如果是book标签结束
                                if ("device" == parser.name) {
                                    //置空
                                    device = null
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

            loggerD(msg="最小宽度："+config.minWidth)
            loggerD(msg="fakeAppList："+config.fakeAppList)
        } catch (e: Exception) {
            loggerE(e = e, msg = "获取配置文件失败")
        }

        loadZygote {
            findClass("android.view.Display").hook {
                injectMember {
                    method {
                        name = "updateDisplayInfoLocked"
                        emptyParam()
                    }
                    afterHook {
                        //Toast.makeText(appContext, "DPI Hooking", Toast.LENGTH_SHORT).show();

                        // Density for this package is overridden, change density
                        loggerD(msg = "updateDisplayInfoLocked hook")
                        field { name = "mDisplayInfo" }.get(instance).current()
                            ?.field { name = "logicalDensityDpi" }?.set(systemContext.resources.displayMetrics.widthPixels*160/config.minWidth)
                    }
                }
            }

            if (config.fakeAppList) {
                "android.app.ApplicationPackageManager".hook {
                    injectMember {
                        method {
                            name = "getInstalledPackages"
                            param(IntType)
                        }
                        afterHook {
                            loggerD(msg = "getInstalledPackages hook")
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
                                val appList = pmList.asSequence()
                                    .map {
                                        pm.getPackageInfo(
                                            it,
                                            PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS
                                        )
                                    }
                                    .filter { it.applicationInfo.sourceDir != null }
                                    .toList()
                                //loggerD(msg = "[appList]"+gson.toJson(appList))
                                result = appList
                            } catch (t: Throwable) {
                                loggerE(msg = "[应用列表]", e = t)
                            }
                        }
                    }

                    injectMember {
                        method {
                            name = "getInstalledApplications"
                            param(IntType)
                        }
                        afterHook {
                            loggerD(msg = "getInstalledApplications hook")
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
                                val appList = pmList.asSequence()
                                    .map {
                                        pm.getPackageInfo(
                                            it,
                                            PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS
                                        ).applicationInfo
                                    }
                                    .filter { it.sourceDir != null }
                                    .toList()
                                result = appList
                            } catch (t: Throwable) {
                                loggerE(msg = "[应用列表]", e = t)
                            }
                        }
                    }
                }
            }
        }

        loadApp {
            onAppLifecycle {
                onCreate {
                    // this 就是当前 Application
                    Toast.makeText(appContext, "「应用配置」运行中\r\n该配置由「${config.author}」提供", Toast.LENGTH_SHORT).show()
                    loggerD(msg = "「应用配置」运行中，当前配置由「${config.author}」提供")
                }
            }

            "android.content.ContextWrapper".hook {
                injectMember {
                    method {
                        name = "attachBaseContext"
                        paramCount = 1
                    }
                    beforeHook {
                        loggerD(msg = "ContextWrapper hook")
                        var context = args().first().cast<Context?>()
                        val res: Resources? = context?.resources
                        val configure = Configuration(res?.configuration)
                        val runningMetrics: DisplayMetrics? = res?.displayMetrics
                        val newMetrics: DisplayMetrics?
                        if (runningMetrics != null) {
                            newMetrics = DisplayMetrics()
                            newMetrics.setTo(runningMetrics)
                        } else {
                            newMetrics = res?.displayMetrics
                        }
                        newMetrics?.density = runningMetrics?.widthPixels?.div(config.minWidth.toFloat())
                        newMetrics?.densityDpi = runningMetrics?.widthPixels?.times(160)
                            ?.div(config.minWidth)
                        configure.current().field { name = "densityDpi" }.set(runningMetrics?.widthPixels?.times(160)
                            ?.div(config.minWidth))
                        context = context?.createConfigurationContext(configure)
                        args().first().set(context)
                    }
                }
            }
        }
    }
}

class Config {
    var minWidth=320
    var fakeAppList=true
    var author="吉王义昊"
}