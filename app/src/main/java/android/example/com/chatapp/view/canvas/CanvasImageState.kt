package android.example.com.chatapp.view.canvas

import android.content.Context
import android.example.com.chatapp.util.PixelsToDpUtil
import android.example.com.chatapp.util.getRandomColor
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

class CanvasImageState(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet) {

    private var path: android.graphics.Path = Path()
    private var paint: Paint = Paint()
    private var textPaint: TextPaint = TextPaint()
    private var canvas: Canvas = Canvas()
    private lateinit var bitmap: Bitmap
    private var canPaint = false

    init {
        configTextPaint()
        configPaint()
    }

    private fun configPaint() {
        paint.color = Color.WHITE
        paint.isAntiAlias = true
        paint.strokeWidth = 20f
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
    }

    private fun configTextPaint() {
        textPaint.color = Color.WHITE
        textPaint.textSize = PixelsToDpUtil.convertPixelsToDp(32, context)
        textPaint.style = Paint.Style.FILL
        textPaint.setShadowLayer(1.5f, 0f, 1.5f, Color.BLACK)
    }

    fun setRandomColorPaint() {
        paint.color = getRandomColor()
    }

    fun enablePainting() {
        canPaint = !canPaint
    }

    fun disablePainting() {
        canPaint = false
    }

    fun writeTextOnCanvas(text: String) {
        val builder = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.0f)
            .setIncludePad(false)
        val staticLayout = builder.build()
        canvas.save()

        val dy = (canvas.height / 2).toFloat() - (staticLayout.height / 2).toFloat()

        canvas.translate(0f, dy)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(bitmap, 0f, 0f, paint)
        if (canPaint) {
            canvas?.drawPath(path, paint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (canPaint) {
            if (event != null) {
                val touchX = event.x
                val touchY = event.y
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        path.moveTo(touchX, touchY)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        path.lineTo(touchX, touchY)
                    }
                    MotionEvent.ACTION_UP -> {
                        path.lineTo(touchX, touchY)
                        canvas.drawPath(path, paint)
                        path.reset()
                    }
                    else -> return false
                }
                invalidate()
            }
            return true
        } else {
            return false
        }
    }

}