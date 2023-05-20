package com.coderzf1.colorpicker

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coderzf1.colorpicker.ColorSpectrumPalette.OnColorSpectrumPicked
import com.coderzf1.colorpicker.databinding.ActivityMainBinding
import java.util.Locale

private fun SeekBar.onSizeChange(callback: () -> Unit) {
    addOnLayoutChangeListener(object : OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View?,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int,
        ) {
            view?.removeOnLayoutChangeListener(this)
            if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) {
                callback()
            }
        }
    })
}

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val _selColor: MutableLiveData<Int> = MutableLiveData()
    val selColor: LiveData<Int> = _selColor

    val _chosenColor:MutableLiveData<Int> = MutableLiveData()
    val chosenColor:LiveData<Int> = _chosenColor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.apply {
            setContentView(root)
            selColor.observe(this@MainActivity) { color ->
                createAndSetTrack(color)

            }
            this@MainActivity.chosenColor.observe(this@MainActivity){color ->
                chosenColor.background = ColorDrawable(color)
                hexValue.text = colorToHTMLColor(color)
            }

            val colorSpectrumListener = object: OnColorSpectrumPicked{
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onColorSpectrumPicked(color: Int) {
                    _selColor.postValue(color)
                    _chosenColor.postValue(getChosenColor(color, binding.vibrance.progress.toFloat()/100))
                }
            }
            picker.setOnColorSpectrumPicked(colorSpectrumListener)
            vibrance.onSizeChange {
                _selColor.postValue(Color.WHITE)
            }
            vibrance.setOnSeekBarChangeListener(object:OnSeekBarChangeListener{
                override fun onProgressChanged(view: SeekBar?, value: Int, p2: Boolean) {
                    _chosenColor.postValue(getChosenColor(selColor.value!!,value.toFloat()/100))

                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })
        }
    }


    fun colorToHTMLColor(color: Int): String? {
        val a = Color.alpha(color)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val hex = String.format("#%02x%02x%02x%02x", a, r, g, b)
        return hex.uppercase(Locale.getDefault())
    }


    private fun dpToPx(dp:Int):Int{
        return (dp + resources.displayMetrics.density).toInt()
    }

    fun getChosenColor(@ColorInt color: Int, vibrancy:Float): Int {
        val f = FloatArray(3)
        Color.colorToHSV(color, f)
        val vibrancy:Float = vibrancy
        val newF: FloatArray = floatArrayOf(f[0], f[1], vibrancy)
        return Color.HSVToColor(newF)
    }

    private fun createAndSetTrack(color:Int){
        val gradient = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(Color.BLACK, color))
        val drawables:Array<Drawable> = arrayOf(gradient)
        val ld = LayerDrawable(drawables)
        binding.vibrance.progressDrawable = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.vibrance.progressDrawable = ld
            val bounds = binding.vibrance.progressDrawable.copyBounds()
            val actualTop = (bounds.bottom - bounds.top) / 2 - dpToPx(24)/2
            binding.vibrance.progressDrawable.setBounds(bounds.left,actualTop,bounds.right,actualTop + dpToPx(24))
        }
    }
}

