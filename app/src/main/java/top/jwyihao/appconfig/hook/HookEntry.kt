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
import android.os.Build
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.Toast
import android.view.Display
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.log.YukiHookLogger
import com.highcapable.yukihookapi.hook.log.loggerD
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
import com.highcapable.yukihookapi.hook.xposed.bridge.event.YukiXposedEvent
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import java.lang.reflect.Field


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
              loggerD(msg = "成功 hook 方法")
              field { name = "mDisplayInfo" }.get(instance)?.current()?.field { name = "logicalDensityDpi" }?.set(dpi)
              //DisplayClass.field { name = "mDisplayInfo" }?.get(instance)?.any()
              //  ?.current()?.field { name = "logicalDensityDpi" }?.set(dpi)
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
      loggerD(msg = "搜寻入口activity中"+packageName)
      val pm: PackageManager? = systemContext?.getPackageManager()
      val intent: Intent = Intent(Intent.ACTION_MAIN, null);
      intent.setPackage(packageName);
      val infos: List<ResolveInfo>? = pm?.queryIntentActivities(intent, PackageManager.MATCH_ALL)
      infos?.forEach {
        loggerD(msg = "[activtiyName]"+it.activityInfo.name);
      }
      ActivityClass.hook {
        injectMember {
          method {
            name = "onCreate"
            paramCount = 1
            returnType = UnitType
          }
          afterHook {
            Toast.makeText(appContext, "『应用配置』运行中", Toast.LENGTH_SHORT).show();
            loggerD(msg = "『应用配置』运行中")
            YukiHookLogger.saveToFile("/sdcard/Android/data/" + packageName + "/appconfig.log")
            YukiHookLogger.clear()
          }
        }
      }

      findClass("android.content.ContextWrapper").hook {
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
              config.current()?.field { name = "densityDpi" }?.set(dpi)
            }
            context = context?.createConfigurationContext(config);
            args().first().set(context)
          }
        }
      }
    }

  }
}
