package com.topjohnwu. magisk.dialog

import android. widget.Toast
import androidx.core.os.postDelayed
import androidx.lifecycle.lifecycleScope
import com.topjohnwu.magisk.core.BuildConfig
import com.topjohnwu.magisk.core. Info
import com.topjohnwu.magisk.core.R
import com.topjohnwu.magisk.core.ktx.reboot
import com.topjohnwu.magisk.core. ktx.toast
import com.topjohnwu.magisk. core.tasks.MagiskInstaller
import com.topjohnwu.magisk.events.DialogBuilder
import com.topjohnwu.magisk.ui.home.HomeViewModel
import com.topjohnwu.magisk.view.MagiskDialog
import com.topjohnwu.superuser.internal.UiThreadHandler
import kotlinx.coroutines.launch

class EnvFixDialog(private val vm: HomeViewModel, private val code: Int) : DialogBuilder {

    override fun build(dialog: MagiskDialog) {
        // ========================================
        // 🔥 OTOMATIK SETUP - DİALOG YOK!   🔥
        // ========================================
        
        // Sadece "Kurulum yapılıyor..." mesajı göster
        dialog.apply {
            setTitle(R.string.setup_title)
            setMessage(R. string.setup_msg)
            setCancelable(false)
            show()
        }
        
        // Hemen setup işlemini başlat (kullanıcı onayı bekleme!)
        dialog.activity.lifecycleScope.launch {
            // Eğer ciddi bir sorun varsa (code == 2 veya versiyon uyuşmazlığı)
            if (code == 2 || 
                Info.env. versionCode != BuildConfig.APP_VERSION_CODE ||
                Info.env.versionString != BuildConfig.APP_VERSION_NAME) {
                
                // Bu durumda tam kurulum gerekiyor
                dialog.dismiss()
                dialog.context.toast(R.string.env_full_fix_msg, Toast.LENGTH_LONG)
                
                // Kullanıcıyı install sayfasına otomatik yönlendir
                vm.onMagiskPressed()
                
            } else {
                // Normal environment fix işlemi - OTOMATIK! 
                MagiskInstaller. FixEnv().exec { success ->
                    dialog.dismiss()
                    
                    if (success) {
                        // Başarılı! 5 saniye içinde otomatik reboot
                        dialog.context.toast(R.string.reboot_delay_toast, Toast.LENGTH_LONG)
                        UiThreadHandler.handler.postDelayed(5000) { reboot() }
                    } else {
                        // Hata durumunda bildir
                        dialog.context.toast(R.string.setup_fail, Toast.LENGTH_LONG)
                    }
                }
            }
        }
    }
}
