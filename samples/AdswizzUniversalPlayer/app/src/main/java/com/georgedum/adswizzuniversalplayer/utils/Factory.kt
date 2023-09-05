package com.georgedum.adswizzuniversalplayer.utils

import android.content.Context
import com.georgedum.adswizzuniversalplayer.viewmodels.MainActivityModel

object Factory {
    private var _mainModel: MainActivityModel? = null

    fun getMainModelInstance(): MainActivityModel{
        synchronized(this){
            if (_mainModel == null ){
                _mainModel = MainActivityModel()
            }
        }
        return _mainModel !!
    }


}