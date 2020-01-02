package app.practice.mycontacts.application

import android.app.Application
import app.practice.mycontacts.BuildConfig
import timber.log.Timber

class ContactsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    /**
     * Initializes Timber Library for logging with DebugTree
     * only if the [BuildConfig] is of type [BuildConfig.DEBUG]
     */
    private fun initTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}