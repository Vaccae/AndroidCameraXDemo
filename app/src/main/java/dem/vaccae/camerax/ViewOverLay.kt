package dem.vaccae.camerax

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.red

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间：2020-11-26 14:51
 * 功能模块说明：
 */
class ViewOverLay constructor(context: Context?, attributeSet: AttributeSet?) :
    View(context, attributeSet) {

    private var mText: String? = null
    private var mPoint: PointF? = null

    private val textpaint = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context!!, android.R.color.holo_blue_light)
        strokeWidth = 10f
        textSize = 150f
        isFakeBoldText = true
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mText?.let {
            canvas?.drawText(it, mPoint!!.x, mPoint!!.y, textpaint)
        }

    }

    fun drawText(str: String, ptr: PointF) {
        mText = str
        mPoint = ptr;
        invalidate()
    }
}