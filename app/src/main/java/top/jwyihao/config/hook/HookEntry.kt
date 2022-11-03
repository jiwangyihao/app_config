package top.jwyihao.config.hook

import android.app.Activity
import android.content.Intent
import android.widget.Button
import android.app.AlertDialog
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.applyModuleTheme
import com.highcapable.yukihookapi.hook.factory.registerModuleAppActivities
import com.highcapable.yukihookapi.hook.type.android.ActivityClass
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
            ActivityClass.hook { 
                injectMember { 
                    method { 
                        name = "onCreate"
                        param(BundleClass)
                        returnType = UnitType
                    }
                    afterHook {
                        // Your code here.
                        AlertDialog.Builder(instance())
                                            .setTitle("Hooked")
                                            .setMessage("I am hook!")
                                            .setPositiveButton("OK", null)
                                            .show()
                    }
                }
            }
        }
    }
}
