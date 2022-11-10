package top.jwyihao.appconfig.hook

import android.app.Activity
import android.app.AlertDialog
import android.content.res.Configuration
import android.content.res.Resources
import android.content.Intent
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.Toast
import android.view.Display
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.log.YukiHookLogger
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.applyModuleTheme
import com.highcapable.yukihookapi.hook.factory.registerModuleAppActivities
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.type.android.ActivityClass
import com.highcapable.yukihookapi.hook.type.android.ResourcesClass
import com.highcapable.yukihookapi.hook.type.android.ConfigurationClass
import com.highcapable.yukihookapi.hook.type.android.DisplayMetricsClass
import com.highcapable.yukihookapi.hook.type.android.DisplayClass
import com.highcapable.yukihookapi.hook.type.java.StringArrayClass
import com.highcapable.yukihookapi.hook.type.java.StringType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.xposed.bridge.event.YukiXposedEvent
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import java.lang.reflect.Field
import okio.buffer
import okio.source
import com.google.gson.Gson


@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

  override fun onInit() = configs {
    // Your code here.
    debugLog {
      isRecord = true
    }
  }

  override fun onHook() = encase {
    // Your code here.
    val dpi: Int = 189

    loadZygote {

      findClass("android.view.Display").hook {
        injectMember {
          method {
            name = "updateDisplayInfoLocked"
            emptyParam()
          }
          afterHook {
            //Toast.makeText(appContext, "DPI Hooking", Toast.LENGTH_SHORT).show();

            if (dpi > 0) {
              // Density for this package is overridden, change density
              loggerD(msg = "updateDisplayInfoLocked hook")
              field { name = "mDisplayInfo" }.get(instance).current()?.field { name = "logicalDensityDpi" }?.set(dpi)
            }
          }
        }
      }
      
      
      "android.app.ApplicationPackageManager".hook {
        injectMember {
          method {
            name = "getInstalledPackages"
            param(IntType)
          }
          afterHook {
            loggerD(msg = "获取应用列表方法 hook")
            try {
              val pm: PackageManager = systemContext.getPackageManager()
              val pmList = mutableListOf<String>()
              val process = Runtime.getRuntime().exec("pm list packages")
              process.inputStream.source().buffer().use { bs ->
                while (true) {
                  bs.readUtf8Line()?.trim()?.let { line ->
                    if (line.startsWith("package:")) {
                      line.removePrefix("package:").takeIf { removedPrefix -> removedPrefix.isNotBlank() }
                        ?.let { pmList.add(it) }
                    }
                  } ?: break
                }
              }
              var appList = pmList.asSequence()
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
              loggerE(msg = "[应用列表]",e = t)
              //Timber.w(t)
              //return emptyList()
            }
          }
        }
      }

      /*
      ActivityClass.hook {
        injectMember {
          method {
            name = "onCreate"
            paramCount = 1
            returnType = UnitType
          }
          afterHook {
            Toast.makeText(appContext, "『应用配置』运行中", Toast.LENGTH_SHORT).show();
          }
        }
      }
      */
    }

    loadApp {
      //var mainActivityName: String = ""
      //loggerD(msg = "搜寻中"+packageName)
      
      val gson: Gson = Gson()
      
      
      
      onAppLifecycle {
        onCreate {
          // this 就是当前 Application
          Toast.makeText(appContext, "『应用配置』运行中", Toast.LENGTH_SHORT).show();
          loggerD(msg = "『应用配置』运行中")
        }
      }
      
      ActivityClass.hook {
        injectMember {
          method {
            name = "onPause"
            emptyParam()
          }
          afterHook {
            YukiHookLogger.saveToFile("/sdcard/Android/data/" + packageName + "/appconfig.log")
            YukiHookLogger.clear()
          }
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
            var context: Context? = args().first().cast<Context?>();
            var res: Resources? = context?.getResources();
            var config: Configuration = Configuration(res?.getConfiguration());
            var runningMetrics: DisplayMetrics? = res?.getDisplayMetrics();
            var newMetrics: DisplayMetrics?;
            if (runningMetrics != null) {
              newMetrics = DisplayMetrics();
              newMetrics.setTo(runningMetrics);
            } else {
              newMetrics = res?.getDisplayMetrics();
            }
            if (dpi > 0) {
              newMetrics?.density = dpi / 160f;
              newMetrics?.densityDpi = dpi;
              config.current().field { name = "densityDpi" }?.set(dpi)
            }
            context = context?.createConfigurationContext(config);
            args().first().set(context)
          }
        }
      }
    }

  }
}
