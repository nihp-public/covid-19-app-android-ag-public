package uk.nhs.nhsx.covid19.android.app.payment

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_isolation_payment.isolationPaymentButton
import kotlinx.android.synthetic.main.view_toolbar_primary.toolbar
import uk.nhs.nhsx.covid19.android.app.R
import uk.nhs.nhsx.covid19.android.app.common.BaseActivity
import uk.nhs.nhsx.covid19.android.app.util.viewutils.setNavigateUpToolbar
import uk.nhs.nhsx.covid19.android.app.util.viewutils.setOnSingleClickListener

class IsolationPaymentActivity : BaseActivity(R.layout.activity_isolation_payment) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setNavigateUpToolbar(
            toolbar,
            R.string.isolation_payment_heading,
            upIndicator = R.drawable.ic_arrow_back_white
        )

        isolationPaymentButton.setOnSingleClickListener {
            startActivityForResult(
                Intent(this, RedirectToIsolationPaymentWebsiteActivity::class.java),
                REQUEST_CODE_URL_FETCHED
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_URL_FETCHED) {
            setResult(RESULT_OK)
            finish()
        }
    }

    companion object {
        const val REQUEST_CODE_URL_FETCHED = 1234
    }
}
