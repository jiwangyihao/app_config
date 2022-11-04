package top.jwyihao.appconfig.hook

import android.app.Activity
import android.app.AlertDialog
import android.content.res.Configuration
import android.content.res.Resources
import android.content.Intent
import android.os.Build
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.Toast
import android.view.Display
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
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
  }

  override fun onHook() = encase {
    // Your code here.
    loadZygote {

      DisplayClass.hook {
        injectMember {
          method {
            name = "updateDisplayInfoLocked"
            emptyParam()
          }
          afterHook {
            //Toast.makeText(appContext, "DPI Hooking", Toast.LENGTH_SHORT).show();

            val dpi: Int = 320
            if (dpi > 0) {
              // Density for this package is overridden, change density
              instance.current()?.field { name = "mDisplayInfo" }?.get(instance).any()
                .current()?.field { name = "logicalDensityDpi" }?.set(dpi)
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
      ResourcesClass.hook {
        injectMember {
          method {
      name = "getConfiguration"
      emptyParam()
          }
          afterHook {
            Toast.makeText(appContext, "Resources", Toast.LENGTH_SHORT).show();
          }
        }
      }
      ResourcesClass.hook {
        injectMember {
          method {
            name = "updateConfiguration"
            //param(ConfigurationClass,DisplayMetricsClass)
            paramCount = 2
          }
          beforeHook {
            // Your code here.
            Toast.makeText(appContext, "『应用配置』运行中", Toast.LENGTH_SHORT).show();

            var configuration: Configuration? = Configuration(args().first().cast<Configuration?>())
            val dpi = 320

            var displayMetrics: DisplayMetrics? = null
            if (args().last().cast<DisplayMetrics?>() != null) {
              displayMetrics = DisplayMetrics()
              displayMetrics.setTo(args().last().cast<DisplayMetrics?>())
              args().last().set(displayMetrics)
            } else {
              displayMetrics = instance<Resources>().displayMetrics
            }
            if (displayMetrics != null) {
              displayMetrics.density = dpi / 160f
              displayMetrics.densityDpi = dpi
              if (Build.VERSION.SDK_INT >= 17) {
                configuration?.current()?.field { name = "densityDpi" }?.set(dpi)
              }
            }

            args().first().set(configuration)
          }
        }
      }
      */
    }
  }
}
