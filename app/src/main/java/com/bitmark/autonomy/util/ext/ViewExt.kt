/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Handler
import android.util.TypedValue
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.bitmark.autonomy.logging.Tracer

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

fun TextView.setTextSize(sp: Int) {
    val screenWidth = context.screenWidth
    val density = context.resources.displayMetrics.density
    val convertedSp = screenWidth * sp / (density * 360)
    setTextSize(TypedValue.COMPLEX_UNIT_SP, convertedSp)
}

fun ImageView.setImageResource(resName: String) {
    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
    setImageResource(resId)
}