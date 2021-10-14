package com.plcoding.doodlekong.ui.custom_view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.plcoding.doodlekong.R
import kotlin.properties.Delegates

class CustomImageRadioButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatRadioButton(context, attrs) {


    private var uncheckedDrawable: VectorDrawableCompat? = null
    private var checkedDrawable: VectorDrawableCompat? = null

    private var viewWidth by Delegates.notNull<Int>()
    private var viewHeight by Delegates.notNull<Int>()

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomImageRadioButton, 0, 0)
            .apply {
                try {
                    val uncheckedID =
                        getResourceId(R.styleable.CustomImageRadioButton_uncheckedDrawable, 0)
                    val checkedID =
                        getResourceId(R.styleable.CustomImageRadioButton_checkedDrawable, 0)

                    if (uncheckedID != 0) {
                        uncheckedDrawable =
                            VectorDrawableCompat.create(resources, uncheckedID, null)
                    }
                    if (checkedID != 0) {
                        checkedDrawable =
                            VectorDrawableCompat.create(resources, checkedID, null)
                    }
                } finally {
                    recycle()
                }
            }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            if (!isChecked) {
                uncheckedDrawable?.setBounds(
                    paddingLeft,
                    paddingTop,
                    viewHeight - paddingRight,
                    viewHeight - paddingBottom
                )
                uncheckedDrawable?.draw(canvas)
            } else {
                checkedDrawable?.setBounds(
                    paddingLeft,
                    paddingTop,
                    viewHeight - paddingRight,
                    viewHeight - paddingBottom
                )
                checkedDrawable?.draw(canvas)
            }
        }
    }

}