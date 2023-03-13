package com.adswizz.podcastsample

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout


class MyCustomView : FrameLayout {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }
}