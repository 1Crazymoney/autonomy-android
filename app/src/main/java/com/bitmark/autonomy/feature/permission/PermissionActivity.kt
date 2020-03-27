/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.permission

import android.Manifest
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.risklevel.RiskLevelActivity
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.openAppSetting
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_permission.*
import javax.inject.Inject


class PermissionActivity : BaseAppCompatActivity() {

    companion object {
        const val LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var logger: EventLogger

    override fun layoutRes(): Int = R.layout.activity_permission

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        val rxPermission = RxPermissions(this)

        ivRequestLocation.setSafetyOnclickListener {
            rxPermission.requestEach(LOCATION_PERMISSION)
                .subscribe({ permission ->
                    when {
                        permission.granted -> logger.logEvent(Event.LOCATION_PERMISSION_GRANTED)
                        permission.shouldShowRequestPermissionRationale -> logger.logEvent(Event.LOCATION_PERMISSION_DENIED)
                        else -> {
                            logger.logEvent(Event.LOCATION_PERMISSION_DENIED)
                            navigator.openAppSetting(this)
                        }
                    }
                }, {})
        }

        layoutNext.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(RiskLevelActivity::class.java)
        }

        layoutBack.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}