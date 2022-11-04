@file:Suppress("SetTextI18n")

package top.jwyihao.appconfig.ui.activity

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.YukiHookAPI
import top.jwyihao.appconfig.BuildConfig
import top.jwyihao.appconfig.R
import top.jwyihao.appconfig.databinding.ActivityMainBinding
import top.jwyihao.appconfig.ui.activity.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun onCreate() {
        refreshModuleStatus()
        binding.mainTextVersion.text = getString(R.string.module_version, BuildConfig.VERSION_NAME)
        binding.hideIconInLauncherSwitch.isChecked = isLauncherIconShowing.not()
        binding.hideIconInLauncherSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) hideOrShowLauncherIcon(isChecked)
        }
        // Your code here.
    }

    /**
     * Hide or show launcher icons
     *
     * - You may need the latest version of LSPosed to enable the function of hiding launcher
     *   icons in higher version systems
     *
     * 隐藏或显示启动器图标
     *
     * - 你可能需要 LSPosed 的最新版本以开启高版本系统中隐藏 APP 桌面图标功能
     * @param isShow Whether to display / 是否显示
     */
    private fun hideOrShowLauncherIcon(isShow: Boolean) {
        packageManager?.setComponentEnabledSetting(
            ComponentName(packageName, "${BuildConfig.APPLICATION_ID}.Home"),
            if (isShow) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * Get launcher icon state
     *
     * 获取启动器图标状态
     * @return [Boolean] Whether to display / 是否显示
     */
    private val isLauncherIconShowing
        get() = packageManager?.getComponentEnabledSetting(
            ComponentName(packageName, "${BuildConfig.APPLICATION_ID}.Home")
        ) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED

    /**
     * Refresh module status
     *
     * 刷新模块状态
     */
    private fun refreshModuleStatus() {
        binding.mainLinStatus.setBackgroundResource(
            when {
                YukiHookAPI.Status.isModuleActive -> R.drawable.bg_green_round
                else -> R.drawable.bg_dark_round
            }
        )
        binding.mainImgStatus.setImageResource(
            when {
                YukiHookAPI.Status.isModuleActive -> R.mipmap.ic_success
                else -> R.mipmap.ic_warn
            }
        )
        binding.mainTextStatus.text = getString(
            when {
                YukiHookAPI.Status.isModuleActive -> R.string.module_is_activated
                else -> R.string.module_not_activated
            }
        )
        binding.mainTextApiWay.isVisible = YukiHookAPI.Status.isModuleActive
        when {
            YukiHookAPI.Status.executorVersion > 0 ->
                binding.mainTextApiWay.text =
                    "Activated by ${YukiHookAPI.Status.executorName} API ${YukiHookAPI.Status.executorVersion}"
            YukiHookAPI.Status.isTaiChiModuleActive -> binding.mainTextApiWay.text = "Activated by TaiChi"
            else -> binding.mainTextApiWay.text = "Activated by anonymous"
        }
    }
}