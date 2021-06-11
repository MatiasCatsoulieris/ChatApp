package android.example.com.chatapp.view.canvas

import android.content.Context
import android.example.com.chatapp.util.PixelsToDpUtil
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

class CanvasTextState(context: Context, attributeSet: AttributeSet): View(context, attributeSet) {
    private lateinit var canvas: Canvas
    private lateinit var bitmap: Bitmap
    private lateinit var textPaint: TextPaint

    init {
        initialize()
        configTextPaint()
    }

    private fun initialize() {
        textPaint = TextPaint()
    }

    private fun configTextPaint() {
        textPaint.color = Color.WHITE
        textPaint.textSize = PixelsToDpUtil.convertPixelsToDp(32, context)
        textPaint.style = Paint.Style.FILL
        textPaint.setShadowLayer(1.5f,0f,1.5f, Color.BLACK)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(bitmap,0f,0f,null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
    }

    fun changeTypeFace(typeFace: Typeface) {
        textPaint.typeface = typeFace
    }

    fun writeTextOnCanvas(text: String) {
        val builder = StaticLayout.Builder.obtain(text, 0 , text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f,1.0f)
            .setIncludePad(false)
        val staticLayout = builder.build()
        canvas.save()

        val dy = (canvas.height/ 2).toFloat() - (staticLayout.height / 2 ).toFloat()

        canvas.translate(0f,dy)
        staticLayout.draw(canvas)
        canvas.restore()
    }


}