package uk.nhs.nhsx.covid19.android.app.widgets

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import androidx.annotation.StringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import uk.nhs.nhsx.covid19.android.app.R
import uk.nhs.nhsx.covid19.android.app.appComponent
import uk.nhs.nhsx.covid19.android.app.util.DistrictAreaStringProvider
import uk.nhs.nhsx.covid19.android.app.util.viewutils.openUrl
import uk.nhs.nhsx.covid19.android.app.util.viewutils.setOnSingleClickListener
import uk.nhs.nhsx.covid19.android.app.util.viewutils.setUpOpensInBrowserWarning
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class LinkTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : UnderlinedTextView(context, attrs, defStyleAttr), CoroutineScope {

    lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    @Inject
    lateinit var districtAreaStringProvider: DistrictAreaStringProvider

    private var linkUrl: Int = 0

    private var districtAreaLinkUrl: Int? = null

    init {
        context.applicationContext.appComponent.inject(this)
        applyAttributes(context, attrs)
        setUpOpensInBrowserWarning()
        setOnSingleClickListener {
            districtAreaLinkUrl?.let {
                getActivity()?.openUrl(it)
            }
        }
    }

    fun setLinkUrl(@StringRes linkUrl: Int) {
        this.linkUrl = linkUrl
        updateDistrictAreaLinkUrl()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        job = Job()
        updateDistrictAreaLinkUrl()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job.cancel()
    }

    private fun updateDistrictAreaLinkUrl() {
        if (this::job.isInitialized) {
            launch {
                districtAreaLinkUrl = districtAreaStringProvider.provide(linkUrl)
            }
        }
    }

    private fun applyAttributes(context: Context, attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LinkTextView,
            0,
            0
        ).apply {
            linkUrl = getResourceId(R.styleable.LinkTextView_linkUrl, 0)

            recycle()
        }
    }

    private fun getActivity(): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }
}
