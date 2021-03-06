/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.logging.Tracer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener

fun View.gone(withAnim: Boolean = false) {
    if (withAnim) {
        animate().alpha(0.0f).setDuration(250)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    visibility = View.GONE
                }
            })
    } else {
        visibility = View.GONE
    }

}

fun View.visible(withAnim: Boolean = false) {
    if (withAnim) {
        animate().alpha(1.0f).setDuration(250)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    visibility = View.VISIBLE
                }
            })
    } else {
        visibility = View.VISIBLE
    }
}

fun View.invisible(withAnim: Boolean = false) {
    if (withAnim) {
        animate().alpha(0.0f).setDuration(250)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    visibility = View.INVISIBLE
                }
            })
    } else {
        visibility = View.INVISIBLE
    }
}

fun View.setSafetyOnclickListener(action: (View?) -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {

        var blocked = false

        val handler = Handler()

        override fun onClick(v: View?) {
            if (blocked) return

            blocked = true
            handler.postDelayed({
                blocked = false
            }, 500)
            action.invoke(v)
        }

    })
}

fun View.enable() {
    this.isEnabled = true
}

fun View.disable() {
    this.isEnabled = false
}

fun TextView.setText(@StringRes id: Int) {
    this.text = context.getString(id)
}

fun TextView.setTextColorRes(@ColorRes id: Int) {
    this.setTextColor(ContextCompat.getColor(context, id))
}

fun TextView.setTextColorStateList(@ColorRes id: Int) {
    this.setTextColor(ContextCompat.getColorStateList(context, id))
}

fun WebView.evaluateJs(script: String?, success: () -> Unit = {}, error: () -> Unit = {}) {
    evaluateJavascript(script) { result ->
        if (result.contains("error", true)) {
            Tracer.ERROR.log("WebView.evaluateJs()", "Script: $script, result:$result")
            error()
        } else {
            success()
        }
    }
}

fun WebView.evaluateVerificationJs(
    script: String,
    callback: (Boolean) -> Unit
) {
    evaluateJavascript(script) { value ->
        when {
            value.isBoolean() -> callback(value?.toBoolean() ?: false)
            else -> {
                Tracer.ERROR.log(
                    "WebView.evaluateVerificationJs()",
                    "Script: $script, value: $value"
                )
                callback(false)
            }
        }
    }
}

fun NestedScrollView.scrollToTop(smooth: Boolean = true) =
    if (smooth) smoothScrollTo(0, 0) else scrollTo(0, 0)

fun ImageView.setImageResource(resName: String) {
    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
    setImageResource(resId)
}

fun View.flip(other: View, duration: Long = 500) {
    other.gone()
    this.visible()
    val d = duration / 2
    this.animate().withLayer()
        .rotationX(90f)
        .setInterpolator(DecelerateInterpolator())
        .scaleY(0f)
        .setDuration(d)
        .withEndAction {
            this.gone()
            other.visible()
            other.rotationX = 0f
            other.scaleY = 0f
            other.animate().withLayer()
                .rotationX(0f)
                .scaleY(1f)
                .setDuration(d)
                .setInterpolator(AccelerateInterpolator())
                .start()
        }.start()
}

fun ImageView.load(
    url: String,
    success: () -> Unit = {},
    error: (GlideException?) -> Unit = {},
    placeholder: Int = R.color.black
) =
    Glide.with(context).load(
        url
    ).placeholder(placeholder).listener(object :
        RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: com.bumptech.glide.request.target.Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            error(e)
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: com.bumptech.glide.request.target.Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            success()
            return false
        }

    }).into(this)