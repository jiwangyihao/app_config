package top.jwyihao.config.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
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
