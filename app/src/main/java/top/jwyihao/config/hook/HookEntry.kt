package top.jwyihao.config.hook

import android.app.Activity
import android.content.res.Configuration
import android.content.res.Resources
import android.content.Intent
import android.widget.Button
import android.app.AlertDialog
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.applyModuleTheme
import com.highcapable.yukihookapi.hook.factory.registerModuleAppActivities
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.android.ActivityClass
import com.highcapable.yukihookapi.hook.type.android.ResourcesClass
import com.highcapable.yukihookapi.hook.type.android.BundleClass
import com.highcapable.yukihookapi.hook.type.java.StringArrayClass
import com.highcapable.yukihookapi.hook.type.java.StringType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.highcapable.yukihookapi.hook.xposed.bridge.event.YukiXposedEvent
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit


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
                param(BundleClass)
                returnType = UnitType
              }
              beforeHook {
                // Your code here.
                var configuration: Configuration? = Configuration(args().first().cast<Configuration?>())
                "android.content.res.Configuration".toClass().field("densityDpi") {
                  set(configuration,320)
                }
                args().first().set(configuration)
              }
            }
          }
        }
    }
}
