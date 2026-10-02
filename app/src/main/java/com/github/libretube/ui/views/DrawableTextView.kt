package com.github.libretube.ui.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.use
import com.github.libretube.R

/**
 * TextView with custom sizable drawable support.
 * It may only be used for icons as it gives same width and height to the drawable.
 */
class DrawableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private var drawableStartDimen = 0F
    private var drawableTopDimen = 0F
    private var drawableEndDimen = 0F
    private var drawableBottomDimen = 0F
    private var isAdjusting = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView).use {
            drawableStartDimen = getDimen(it, R.styleable.DrawableTextView_drawableStartDimen)
            drawableTopDimen = getDimen(it, R.styleable.DrawableTextView_drawableTopDimen)
            drawableEndDimen = getDimen(it, R.styleable.DrawableTextView_drawableEndDimen)
            drawableBottomDimen = getDimen(it, R.styleable.DrawableTextView_drawableBottomDimen)

            if (it.hasValue(R.styleable.DrawableTextView_android_gravity)) {
                gravity = it.getInt(
                    R.styleable.DrawableTextView_android_gravity,
                    Gravity.CENTER_VERTICAL
                )
            }
            if (it.hasValue(R.styleable.DrawableTextView_android_drawablePadding)) {
                compoundDrawablePadding = it.getDimensionPixelOffset(
                    R.styleable.DrawableTextView_android_drawablePadding,
                    20
                )
            }
        }
        adjustDrawables()
    }

    private fun getDimen(ta: TypedArray, index: Int): Float {
        return ta.getDimensionPixelOffset(index, 0).toFloat()
    }

    private fun adjustDrawables() {
        if (isAdjusting) return
        isAdjusting = true
        try {
            val drawables = compoundDrawablesRelative
            val adjusted = adjust(drawables)
            super.setCompoundDrawablesRelative(
                adjusted[0],
                adjusted[1],
                adjusted[2],
                adjusted[3]
            )
        } finally {
            isAdjusting = false
        }
    }

    override fun setCompoundDrawablesRelative(
        start: Drawable?,
        top: Drawable?,
        end: Drawable?,
        bottom: Drawable?
    ) {
        if (isAdjusting) {
            super.setCompoundDrawablesRelative(start, top, end, bottom)
            return
        }
        val adjusted = adjust(arrayOf(start, top, end, bottom))
        super.setCompoundDrawablesRelative(
            adjusted[0],
            adjusted[1],
            adjusted[2],
            adjusted[3]
        )
    }

    override fun setCompoundDrawablesRelativeWithIntrinsicBounds(
        start: Drawable?,
        top: Drawable?,
        end: Drawable?,
        bottom: Drawable?
    ) {
        if (isAdjusting) {
            super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
            return
        }
        val adjusted = adjust(arrayOf(start, top, end, bottom))
        super.setCompoundDrawablesRelative(
            adjusted[0],
            adjusted[1],
            adjusted[2],
            adjusted[3]
        )
    }

    override fun setCompoundDrawablesWithIntrinsicBounds(
        left: Drawable?,
        top: Drawable?,
        right: Drawable?,
        bottom: Drawable?
    ) {
        if (isAdjusting) {
            super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
            return
        }
        val adjusted = adjust(arrayOf(left, top, right, bottom))
        super.setCompoundDrawables(
            adjusted[0],
            adjusted[1],
            adjusted[2],
            adjusted[3]
        )
    }

    private fun adjust(drawables: Array<Drawable?>): Array<Drawable?> {
        val dimens = floatArrayOf(
            drawableStartDimen,
            drawableTopDimen,
            drawableEndDimen,
            drawableBottomDimen
        )
        for (i in dimens.indices) {
            val dimen = dimens[i]
            val drawable = drawables[i]
            if (drawable != null && dimen > 0) {
                drawable.setBounds(0, 0, dimen.toInt(), dimen.toInt())
            }
        }
        return drawables
    }
}
