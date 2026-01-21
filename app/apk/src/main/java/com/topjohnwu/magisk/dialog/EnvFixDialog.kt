package com.topjohnwu.magisk.dialog

import android.widget.Toast
import androidx.core.os.postDelayed
import androidx.lifecycle.lifecycleScope
import com.topjohnwu.magisk.core.BuildConfig
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.core.R
import com.topjohnwu.magisk.core.ktx.reboot
import com.topjohnwu.magisk.core.ktx.toast
import com.topjohnwu.magisk.core.tasks.MagiskInstaller
import com.topjohnwu.magisk.events.DialogBuilder
import com.topjohnwu.magisk.ui.home.HomeViewModel
import com.topjohnwu.magisk.view.MagiskDialog
import com.topjohnwu.superuser.internal.UiThreadHandler
import kotlinx.coroutines.launch

class EnvFixDialog(
    private val vm: HomeViewModel,
    private val code: Int
) : DialogBuilder {

    override fun build(dialog: MagiskDialog) {
        dialog.apply {
            setTitle(R.string.setup_title)
            setMessage(R.string.setup_msg)
            setCancelable(false)
        }

        dialog.activity.lifecycleScope.launch {
            if (
                code == 2 ||
                Info.env.versionCode != BuildConfig.APP_VERSION_CODE ||
                Info.env.versionString != BuildConfig.APP_VERSION_NAME
            ) {
                dialog.dismiss()
                dialog.context.toast(
                    R.string.env_full_fix_msg,
                    Toast.LENGTH_LONG
                )
                vm.onMagiskPressed()
            } else {
                MagiskInstaller.FixEnv().exec { success ->
                    dialog.dismiss()
                    dialog.context.toast(
                        if (success) {
                            R.string.reboot_delay_toast
                        } else {
                            R.string.setup_fail
                        },
                        Toast.LENGTH_LONG
                    )

                    if (success) {
                        UiThreadHandler.handler.postDelayed(5000) {
                            reboot()
                        }
                    }
                }
            }
        }
    }
}
