/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.SeekBar
import androidx.lifecycle.Observer
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.feature.location.LocationService
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ChromeCustomTabServiceHandler
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.AreaModelView
import com.bitmark.autonomy.util.modelview.AreaProfileModelView
import com.bitmark.autonomy.util.modelview.FormulaModelView
import com.bitmark.autonomy.util.modelview.toColorRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.layout_area_info.*
import kotlinx.android.synthetic.main.layout_view_source.*
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt


class MainFragment : BaseSupportFragment() {

    companion object {

        private const val BMRK_JUPYTER_NOTEBOOK =
            "https://nbviewer.jupyter.org/github/bitmark-inc/autonomy-api/blob/master/share/jupyter/autonomyFormula.ipynb"

        private const val CORONA_DATA_SCRAPER_PROJECT = "https://coronadatascraper.com"

        private const val AREA_DATA = "area_data"

        private const val AREA_PROFILE = "area_profile"

        private const val FORMULA = "formula"

        private const val MSA_0 = "MSA_0"

        fun newInstance(areaData: AreaModelView? = null) = MainFragment().apply {
            val bundle = Bundle().apply { if (areaData != null) putParcelable(AREA_DATA, areaData) }
            arguments = bundle
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var viewModel: MainFragmentViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var locationService: LocationService

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    @Inject
    internal lateinit var customTabServiceHandler: ChromeCustomTabServiceHandler

    private lateinit var areaProfile: AreaProfileModelView

    private lateinit var formula: FormulaModelView

    private var blocked = false

    private val locationChangedListener = object : LocationService.LocationChangedListener {

        override fun onPlaceChanged(place: String) {
            if (!isMsa0 || place.isEmpty()) return
            tvLocation.text = place
        }

        override fun onLocationChanged(l: Location) {
            if (!isAreaProfileReady() && isMsa0) {
                viewModel.getCurrentAreaProfile()
            }
        }
    }

    private var areaData: AreaModelView? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    var isMsa0 = false
        private set

    fun getAreaId() = areaData?.id

    fun updateAlias(alias: String) {
        areaData?.alias = alias
        tvLocation.text = alias
    }

    private fun isAreaProfileReady() = ::areaProfile.isInitialized

    private fun isFormulaReady() = ::formula.isInitialized

    override fun layoutRes(): Int = R.layout.fragment_main

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        areaData = arguments?.getParcelable(AREA_DATA)
        isMsa0 = areaData == null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(AREA_DATA)) {
                areaData = savedInstanceState.getParcelable(AREA_DATA)
            }
            if (savedInstanceState.containsKey(AREA_PROFILE)) {
                areaProfile = savedInstanceState.getParcelable(AREA_PROFILE)!!
            }
            if (savedInstanceState.containsKey(MSA_0)) {
                isMsa0 = savedInstanceState.getBoolean(MSA_0)
            }
            if (savedInstanceState.containsKey(FORMULA)) {
                formula = savedInstanceState.getParcelable(FORMULA)!!
            }
        }
        super.onViewCreated(view, savedInstanceState)
        locationService.addLocationChangeListener(locationChangedListener)
    }

    override fun onDestroyView() {
        locationService.removeLocationChangeListener(locationChangedListener)
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (areaData != null) outState.putParcelable(AREA_DATA, areaData)
        if (isAreaProfileReady()) outState.putParcelable(AREA_PROFILE, areaProfile)
        if (isFormulaReady()) outState.putParcelable(FORMULA, formula)
        outState.putBoolean(MSA_0, isMsa0)
    }

    override fun onStart() {
        super.onStart()
        customTabServiceHandler.bind()
    }

    override fun onResume() {
        super.onResume()
        if (isMsa0) {
            if (locationService.isPermissionGranted(activity!!)) viewModel.getCurrentAreaProfile()
        } else {
            viewModel.getAreaProfile(areaData!!.id)
        }
        if (isViewSourceShowing()) {
            viewModel.getFormula(Locale.getDefault().langCountry())
        }
    }

    private fun isViewSourceShowing() =
        ::bottomSheetBehavior.isInitialized && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED

    fun closeViewSourcePanel() {
        if (!isViewSourceShowing()) return
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun initComponents() {
        super.initComponents()

        val desc = getString(R.string.the_autonomy_score_is_a_normalized_score)
        val spannableString = SpannableString(desc)
        val underlineText1 = getString(R.string.corona_data_scraper_project)
        var startIndex = desc.indexOf(underlineText1)
        if (startIndex != -1) {
            spannableString.setSpan(
                UnderlineSpan(),
                startIndex,
                startIndex + underlineText1.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        navigator.openChromeTab(context!!, CORONA_DATA_SCRAPER_PROJECT)
                    }

                }, startIndex,
                startIndex + underlineText1.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        val underlineText2 = getString(R.string.autonomy_score_jupyter_notebook)
        startIndex = desc.indexOf(underlineText2)
        if (startIndex != -1) {
            spannableString.setSpan(
                UnderlineSpan(),
                startIndex,
                startIndex + underlineText2.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        navigator.openChromeTab(context!!, BMRK_JUPYTER_NOTEBOOK)
                    }

                }, startIndex,
                startIndex + underlineText2.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        arrayOf(
            R.string.autonomy_score,
            R.string.daily_confirmed_infectious_disease,
            R.string.symptoms_score,
            R.string.behaviors_score
        ).forEach { res ->
            val boldText = getString(res)
            val i = desc.indexOf(boldText)
            if (i != 1) {
                spannableString.setSpan(
                    StyleSpan(Typeface.BOLD),
                    i,
                    i + boldText.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
        }


        tvDesc.text = spannableString
        tvDesc.movementMethod = LinkMovementMethod.getInstance()
        tvDesc.setLinkTextColor(context!!.getColor(R.color.black))
        tvDesc.highlightColor = Color.TRANSPARENT


        if (areaData != null) {
            tvLocation.text = areaData!!.alias
        }

        if (isAreaProfileReady()) {
            showData(areaProfile)
            if (isFormulaReady()) {
                showFormula(areaProfile, formula)
            }
        }

        bottomSheetBehavior = BottomSheetBehavior.from(layoutViewSourceRoot)
        ivScore.setOnClickListener {
            if (!isAreaProfileReady()) return@setOnClickListener
            bottomSheetBehavior.state =
                if (isViewSourceShowing()) {
                    BottomSheetBehavior.STATE_HIDDEN
                } else {
                    BottomSheetBehavior.STATE_EXPANDED
                }
        }

        view!!.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                handleBottomPanelInteraction()
            }

        })

        val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!isAreaProfileReady() || !isFormulaReady()) return
                when (seekBar) {
                    sbCasesWeight -> formula.coefficient.confirms = progress / 100f
                    sbBehaviorsWeight -> formula.coefficient.behaviors = progress / 100f
                    sbSymptomsWeight -> formula.coefficient.symptoms = progress / 100f
                }
                showFormula(areaProfile, formula)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!isAreaProfileReady() || !isFormulaReady()) return
                viewModel.updateFormula(formula)
            }

        }

        sbCasesWeight.setOnSeekBarChangeListener(seekBarChangeListener)
        sbBehaviorsWeight.setOnSeekBarChangeListener(seekBarChangeListener)
        sbSymptomsWeight.setOnSeekBarChangeListener(seekBarChangeListener)

        tvReset.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            viewModel.deleteFormula(Locale.getDefault().displayName)
        }

        tvJupyterNotebook.setSafetyOnclickListener {
            navigator.openChromeTab(context!!, BMRK_JUPYTER_NOTEBOOK)
        }

        customTabServiceHandler.setUrls(arrayOf(BMRK_JUPYTER_NOTEBOOK, CORONA_DATA_SCRAPER_PROJECT))

    }

    private fun handleBottomPanelInteraction() {
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            val context = this@MainFragment.context!!
            val bottomSheetHeight = layoutViewSourceRoot.height
            val triangleVerticalPadding = 2 * context.getDimensionPixelSize(R.dimen.dp_32)

            val triangleYOffset = ivScore.y
            val tvScoreYOffset = tvScore.y
            val tvAppNameXOffset = tvAppName.x
            val triangleHeight = ivScore.height
            val tvScoreHeight = tvScore.height
            val scaledTriangleHeight =
                rootView!!.height - bottomSheetHeight - tvLocation.height - triangleVerticalPadding
            val minScale = scaledTriangleHeight.toFloat() / triangleHeight
            val scaleDelta = 1f - minScale
            val scaledTriangleYOffset = tvLocation.y + triangleVerticalPadding / 2
            val scaledTriangleDeltaHeight = triangleHeight * scaleDelta
            val maxTriangleTranslateY =
                triangleYOffset - scaledTriangleYOffset + scaledTriangleDeltaHeight / 2
            val maxTvScoreTranslateY =
                maxTriangleTranslateY + tvScoreHeight * scaleDelta / 2 - context.getDimensionPixelSize(
                    R.dimen.dp_6
                ) // estimated px to make the tvScore is vertically center
            val maxTvAppNameTranslateX = context.screenWidth / 2 + tvAppName.width / 2
            var lastOffset = 0f

            override fun onSlide(p0: View, offset: Float) {
                if (offset == 0f) {
                    ivScore.scaleX = 1f
                    ivScore.scaleY = 1f
                    tvScore.scaleX = 1f
                    tvScore.scaleY = 1f
                    tvAppName.alpha = 1f
                    ivScore.y = triangleYOffset
                    tvScore.y = tvScoreYOffset
                    tvAppName.x = tvAppNameXOffset
                } else {
                    // calculate scale
                    val scaleRate = 1f - (scaleDelta * offset)
                    ivScore.scaleX = scaleRate
                    ivScore.scaleY = scaleRate
                    tvScore.scaleX = scaleRate
                    tvScore.scaleY = scaleRate

                    // calculate alpha
                    tvAppName.alpha = 1 - offset

                    // calculate translate X for tvAppName
                    val translateXDelta = (lastOffset - offset) * maxTvAppNameTranslateX
                    tvAppName.x -= translateXDelta
                    Log.d("onSlide", "translateXDelta = $translateXDelta")

                    // calculate translate Y
                    val triangleDeltaY = (lastOffset - offset) * maxTriangleTranslateY
                    val tvScoreDeltaY = (lastOffset - offset) * maxTvScoreTranslateY
                    Log.d(
                        "onSlide",
                        "triangleDeltaY = $triangleDeltaY, tvScoreDeltaY = $tvScoreDeltaY,  offset=$offset"
                    )
                    ivScore.y += triangleDeltaY
                    tvScore.y += tvScoreDeltaY
                }
                lastOffset = offset
            }

            override fun onStateChanged(p0: View, state: Int) {
                if (isViewSourceShowing()) {
                    viewModel.getFormula(Locale.getDefault().langCountry())
                }
            }

        })
    }

    override fun observe() {
        super.observe()

        viewModel.getAreaProfileLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    areaProfile = res.data()!!
                    showData(areaProfile)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.AREA_PROFILE_GETTING_ERROR, res.throwable())
                }

                res.isLoading() -> {
                    if (this@MainFragment::areaProfile.isInitialized) return@Observer
                    progressBar.visible()
                }
            }
        })

        viewModel.getFormulaLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    formula = res.data()!!
                    showFormula(areaProfile, formula)
                }

                res.isError() -> {
                    logger.logError(Event.FORMULA_GETTING_ERROR, res.throwable())
                }
            }
        })

        viewModel.deleteFormulaLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBarViewSource.gone()
                    formula = res.data()!!
                    showFormula(areaProfile, formula)
                    blocked = false
                }

                res.isError() -> {
                    progressBarViewSource.gone()
                    logger.logError(Event.FORMULA_DELETE_ERROR, res.throwable())
                    if (connectivityHandler.isConnected()) {
                        dialogController.alert(R.string.error, R.string.could_not_reset_formula)
                    } else {
                        dialogController.showNoInternetConnection()
                    }
                    blocked = false
                }

                res.isLoading() -> {
                    progressBarViewSource.visible()
                    blocked = true
                }
            }
        })

        viewModel.updateFormulaLiveData.asLiveData().observe(this, Observer { res ->
            when {

                res.isSuccess() -> {
                    formula.isDefault = false
                    showFormula(areaProfile, formula)
                }

                res.isError() -> {
                    logger.logError(Event.FORMULA_UPDATE_ERROR, res.throwable())
                }
            }
        })
    }

    private fun showFormula(areaProfile: AreaProfileModelView, formula: FormulaModelView) {
        if (formula.isDefault) {
            tvReset.disable()
        } else {
            tvReset.enable()
        }
        val confirmCoefficient = formula.coefficient.confirms
        val behaviorsCoefficient = formula.coefficient.behaviors
        val symptomCoefficient = formula.coefficient.symptoms
        sbCasesWeight.progress = (confirmCoefficient * 100).roundToInt()
        sbBehaviorsWeight.progress = (behaviorsCoefficient * 100).roundToInt()
        sbSymptomsWeight.progress = (symptomCoefficient * 100).roundToInt()
        tvCasesWeight.text = String.format("%.2f", confirmCoefficient)
        tvBehaviorsWeight.text = String.format("%.2f", behaviorsCoefficient)
        tvSymptomsWeight.text = String.format("%.2f", symptomCoefficient)

        // cases score
        val casesCore = areaProfile.detail.confirmMetric.score
        val roundedCaseScore = if (casesCore < 0f) 0 else casesCore.roundToInt()
        showCasesScore(roundedCaseScore)

        // behavior score
        val behaviorScore = areaProfile.detail.behaviorMetric.score
        showBehaviorScore(behaviorScore.roundToInt())

        // symptom score
        val symptomScore = areaProfile.detail.symptomMetric.score
        showSymptomScore(symptomScore.roundToInt())

        // calculate total score
        val score = calculateScore(
            if (casesCore < 0f) 0f else casesCore,
            confirmCoefficient,
            behaviorScore,
            behaviorsCoefficient,
            symptomScore,
            symptomCoefficient
        ).roundToInt()
        tvScoreViewSource.text = score.toString()
        tvScoreViewSource.setTextColorRes(toColorRes(score))
        showScore(score)
    }

    private fun calculateScore(
        casesCore: Float,
        confirmCoefficient: Float,
        behaviorScore: Float,
        behaviorsCoefficient: Float,
        symptomScore: Float,
        symptomCoefficient: Float
    ) =
        (casesCore * confirmCoefficient + behaviorScore * behaviorsCoefficient + symptomScore * symptomCoefficient)

    private fun showCasesScore(score: Int) {
        tvCasesScore1.text = score.toString()
        tvCasesScore1.setTextColorRes(toColorRes(score))
    }

    private fun showBehaviorScore(score: Int) {
        tvBehaviorsScore1.text = score.toString()
        tvBehaviorsScore1.setTextColorRes(toColorRes(score))
    }

    private fun showSymptomScore(score: Int) {
        tvSymptomsScore1.text = score.toString()
        tvSymptomsScore1.setTextColorRes(toColorRes(score))
    }

    private fun showScore(score: Int) {
        tvScore.text = score.toString()
        ivScore.setImageResource("triangle_%03d".format(if (score > 100) 100 else score))
    }

    private fun showData(profile: AreaProfileModelView) {
        showScore(profile.score.roundToInt())

        tvConfirmedCases.text = profile.confirmed.abbreviate()
        tvConfirmedCasesChange.text = String.format("%.2f%%", abs(profile.confirmedDelta))
        tvConfirmedCasesChange.setTextColorRes(
            when {
                profile.confirmedDelta == 0f -> R.color.white
                profile.confirmedDelta > 0 -> R.color.persian_red
                else -> R.color.apple
            }
        )
        if (profile.confirmedDelta == 0f) {
            ivConfirmedCasesChange.invisible()
        } else {
            ivConfirmedCasesChange.visible()
            ivConfirmedCasesChange.setImageResource(if (profile.confirmedDelta > 0) R.drawable.ic_up_red else R.drawable.ic_down_green)
        }

        tvReportedSymptom.text = profile.symptoms.abbreviate()
        tvReportedSymptomChange.text = String.format("%.2f%%", abs(profile.symptomsDelta))
        tvReportedSymptomChange.setTextColorRes(
            when {
                profile.symptomsDelta == 0f -> R.color.white
                profile.symptomsDelta > 0 -> R.color.persian_red
                else -> R.color.apple
            }
        )
        if (profile.symptomsDelta == 0f) {
            ivReportedSymptomChange.invisible()
        } else {
            ivReportedSymptomChange.visible()
            ivReportedSymptomChange.setImageResource(if (profile.symptomsDelta > 0) R.drawable.ic_up_red else R.drawable.ic_down_green)
        }

        tvHealthyBehavior.text = profile.behaviors.abbreviate()
        tvHealthyBehaviorChange.text = String.format("%.2f%%", abs(profile.behaviorsDelta))
        tvHealthyBehaviorChange.setTextColorRes(
            when {
                profile.behaviorsDelta == 0f -> R.color.white
                profile.behaviorsDelta > 0 -> R.color.apple
                else -> R.color.persian_red
            }
        )
        if (profile.behaviorsDelta == 0f) {
            ivHealthyBehaviorChange.invisible()
        } else {
            ivHealthyBehaviorChange.visible()
            ivHealthyBehaviorChange.setImageResource(if (profile.behaviorsDelta > 0) R.drawable.ic_up_green else R.drawable.ic_down_red)
        }
    }

    override fun onBackPressed(): Boolean {
        return if (isViewSourceShowing()) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            true
        } else {
            super.onBackPressed()
        }
    }
}