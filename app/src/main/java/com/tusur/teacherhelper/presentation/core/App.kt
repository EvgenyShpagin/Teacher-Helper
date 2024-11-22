package com.tusur.teacherhelper.presentation.core

import android.app.Application
import com.tusur.teacherhelper.data.room.db.AppDatabase
import com.tusur.teacherhelper.di.AppModule
import com.tusur.teacherhelper.di.AppModuleImpl

class App : Application() {

    companion object {
        private var _appModule: AppModule? = null
        val module get() = _appModule!!
    }

    override fun onCreate() {
        super.onCreate()
        AppDatabase.initialize(applicationContext)
        _appModule = AppModuleImpl(AppDatabase.get())
    }
}