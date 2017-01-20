package com.monolito.ezshapes

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.View
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.google.android.vending.licensing.AESObfuscator
import com.google.android.vending.licensing.LicenseChecker
import com.google.android.vending.licensing.LicenseCheckerCallback
import com.google.android.vending.licensing.ServerManagedPolicy


class AndroidLauncher : AndroidApplication() {

    final val BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjwc4fLSNvp3OhWMhFNGcKI62OpOTpfcKvR3BsoJnjCrDPLUaJsh0SfiP79ASXZ4vVqSD+7RoLthWxkuFo8bE3SE1kXwybBoEz9NkHx1UrEvrmg3lgMqkr0Q0s+mQMj41N37BKSMMkGg/B4xfDufXFXt1ExZhyumx+T/uVvT7lwzdukaoKKKJx6N0rUED/jznVdxXlxSFLEq/IBGZfvgk02QGbVgc3Hl75kExu1dhmetUc83lJoMPEKWZ0/lyouhn53lpzM8KAUsIuvQLJkTU/6ikfM1ly2v3HCOnz9ctzbXjS+5wrjQw9CfQB4TRw1OD336UM46UEcm6QNpI6WnyrwIDAQAB"
    final val SALT = byteArrayOf(-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95,
        -45, 77, -117, -36, -113, -11, 32, -64, 89)
    lateinit var mLicenseCheckerCallback: LicenseCheckerCallback
    lateinit var mChecker: LicenseChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        initialize(EZShapesGame(), config)
        val decorView = getWindow().getDecorView()
        val uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.setSystemUiVisibility(uiOptions);

        //val deviceId = "dummy" //Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mLicenseCheckerCallback = MyLicenseCheckerCallback()
        mChecker = LicenseChecker(
                this, ServerManagedPolicy(this, AESObfuscator(SALT, getPackageName(), "dummy")), BASE64_PUBLIC_KEY)

        try {
            mChecker.checkAccess(mLicenseCheckerCallback)
        } catch (e: Exception) {
            System.out.println(">>>>>" + e.message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mChecker != null) mChecker.onDestroy()
    }

    private class MyLicenseCheckerCallback : LicenseCheckerCallback {
        override fun applicationError(errorCode: Int) {
        }

        override fun allow(reason: Int): Unit {
        }

        override fun dontAllow(reason: Int): Unit {
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
}