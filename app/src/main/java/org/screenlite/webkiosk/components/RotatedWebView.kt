package org.screenlite.webkiosk.components

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class RotatedWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {
    var appliedRotation: Float = 0f
        set(value) {
            field = value
            rotation = value
            requestLayout()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pivotX = w / 2f
        pivotY = h / 2f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)

        if (appliedRotation % 180f == 0f) {
            setMeasuredDimension(parentWidth, parentHeight)
        } else {
            setMeasuredDimension(parentHeight, parentWidth)
        }
    }
}