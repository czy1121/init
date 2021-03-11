package me.reezy.init

import android.app.Application

interface InitTask {
    fun execute(app: Application)
}