package com.coderzf1.colorpicker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class ColorSpectrumPalette : View,View.OnTouchListener{
    constructor(context:Context):super(context)
    constructor (context:Context, attrs: AttributeSet, defStyle: Int) : super(context,attrs,defStyle)
    constructor(context:Context, attrs:AttributeSet):this(context,attrs,0)
    private lateinit var colorSpectrum:GradientDrawable
    private lateinit var whiteGradient:GradientDrawable
    private lateinit var fullSpectrum:LayerDrawable
    private lateinit var spectrumBitmap:Bitmap
    private lateinit var spectrumCanvas:Canvas
    private var listener:OnColorSpectrumPicked? = null
    private lateinit var outerCursorPaint: Paint
    private lateinit var innerCursorPaint:Paint
    private var touchX: Int? = null
    private var touchY:Int? = null

    init {
        createColorSpectrum()
        createWhiteSpectrum()
        createFullSpectrum()
        createSpectrumBitmap()
        createSpectrumCanvas()
        createCursorPaints()
        setOnTouchListener(this)
    }

    private fun createColorSpectrum(){
        val colorSpectrumColors:IntArray = intArrayOf(Color.RED,Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED)
        colorSpectrum = GradientDrawable()
        colorSpectrum.apply {
            this.colors = colorSpectrumColors
            orientation = GradientDrawable.Orientation.LEFT_RIGHT
            gradientType = GradientDrawable.LINEAR_GRADIENT
        }
    }

    private fun createWhiteSpectrum(){
        val whiteGradientColors:IntArray = intArrayOf(Color.TRANSPARENT, Color.WHITE)

        whiteGradient = GradientDrawable()

        whiteGradient.apply {
            colors = whiteGradientColors
            gradientType = GradientDrawable.LINEAR_GRADIENT
        }
    }

    private fun createFullSpectrum(){
        fullSpectrum = LayerDrawable(arrayOf(colorSpectrum,whiteGradient))
    }

    private fun createSpectrumBitmap(){
        spectrumBitmap = Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)
    }

    private fun createSpectrumCanvas(){
        spectrumCanvas = Canvas(spectrumBitmap)
    }

    private fun createCursorPaints(){
        outerCursorPaint = Paint()
        innerCursorPaint = Paint()
        outerCursorPaint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
            flags = Paint.ANTI_ALIAS_FLAG
        }
        innerCursorPaint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 4f
            flags = Paint.ANTI_ALIAS_FLAG
        }
    }

    fun setOnColorSpectrumPicked(listener:OnColorSpectrumPicked?){
        this.listener = listener
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        fullSpectrum.setBounds(0,0,width,height)
        fullSpectrum.draw(canvas)
        fullSpectrum.draw(spectrumCanvas)
        if(touchX == null || touchY == null) return
        canvas.drawCircle(touchX!!.toFloat(),touchY!!.toFloat(),22f,innerCursorPaint)
        canvas.drawCircle(touchX!!.toFloat(),touchY!!.toFloat(),24f,outerCursorPaint)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        spectrumBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        createSpectrumCanvas()
        invalidate()
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        if (motionEvent == null) return false
        if(motionEvent.action == MotionEvent.ACTION_DOWN || motionEvent.action == MotionEvent.ACTION_MOVE){
            motionEvent.apply {
                Log.d(this@ColorSpectrumPalette.javaClass.name, "onTouch: (${motionEvent.x} , ${motionEvent.y})")
                var chosenColor:Int? = null
                var xx:Int = x.toInt()
                var yy:Int = y.toInt()
                if (x < 0) xx = 0
                if (x > spectrumBitmap.width-1) xx = spectrumBitmap.width-1
                if(y < 0) yy = 0
                if(y > spectrumBitmap.height-1) yy = spectrumBitmap.height-1
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) chosenColor = spectrumBitmap.getColor(xx,yy).toArgb()
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) chosenColor = spectrumBitmap.getPixel(xx,yy)
                if (chosenColor != null) {
                    listener?.onColorSpectrumPicked(chosenColor)
                }
                touchX = xx
                touchY = yy
                invalidate()
            }
            return true
        }
        return super.onTouchEvent(motionEvent)
    }

    interface OnColorSpectrumPicked{
        fun onColorSpectrumPicked(color:Int)
    }

}
