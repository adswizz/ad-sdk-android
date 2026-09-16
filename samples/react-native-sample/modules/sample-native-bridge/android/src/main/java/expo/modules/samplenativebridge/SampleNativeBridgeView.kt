package expo.modules.samplenativebridge

import android.content.Context
import android.graphics.Color
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.views.ExpoView
import com.ad.core.companion.AdCompanionView

class SampleNativeBridgeView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  private val companionView = AdCompanionView(context = context, renderType = AdCompanionView.RenderType.IMAGE_VIEW).also {
    it.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    it.setBackgroundColor(Color.parseColor("#1E293B"))
    addView(it)
  }
}
