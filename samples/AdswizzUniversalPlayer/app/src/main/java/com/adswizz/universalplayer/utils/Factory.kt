package com.adswizz.universalplayer.utils

import com.adswizz.universalplayer.viewmodels.MainActivityModel

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