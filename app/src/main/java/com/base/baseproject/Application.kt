package com.base.baseproject

import android.content.Context
import androidx.multidex.MultiDex
import com.base.common.base.BaseApplication
import timber.log.Timber
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.SSLContext

class Application : BaseApplication() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }else{
            Timber.plant(object : Timber.Tree(){
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

                }
            })
        }

        updateAndroidSecurityProvider()
    }

    private fun updateAndroidSecurityProvider() {
        // To fix the following issue, when run app in cellular data, Apis not working
        // javax.net.ssl.SSLHandshakeException: SSL handshake aborted: ssl=0x7edfc49e08: I/O error during system call, Connection reset by peer
        try {
            ProviderInstaller.installIfNeeded(applicationContext)
            val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, null, null)
            sslContext.createSSLEngine()
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }
    }

}