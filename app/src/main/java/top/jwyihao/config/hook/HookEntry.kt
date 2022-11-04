package top.jwyihao.config.hook

import android.app.Activity
import android.content.res.Configuration
import android.content.res.Resources
import android.content.Intent
import android.widget.Button
import android.app.AlertDialog
import android.widget.Toast
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
          ResourcesClass.hook {
            injectMember {
              method { 
                name = "updateConfiguration"
                param(ConfigurationClass)
              }
              beforeHook {
                // Your code here.
                Toast.makeText(appContext, "『应用配置』运行中",Toast.LENGTH_SHORT).show();
                var configuration: Configuration? = Configuration(args().first().cast<Configuration?>())
                configuration?.current()?.field { name = "densityDpi" }?.set(320)
                args().first().set(configuration)
              }
            }
          }
        }
    }
}
