package uk.nhs.nhsx.covid19.android.app.testordering

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import uk.nhs.nhsx.covid19.android.app.R
import uk.nhs.nhsx.covid19.android.app.appComponent
import uk.nhs.nhsx.covid19.android.app.common.BaseActivity
import uk.nhs.nhsx.covid19.android.app.databinding.ActivityTestOrderingBinding
import uk.nhs.nhsx.covid19.android.app.util.viewutils.setNavigateUpToolbar
import uk.nhs.nhsx.covid19.android.app.util.viewutils.setOnSingleClickListener
import uk.nhs.nhsx.covid19.android.app.util.viewutils.setUpOpensInBrowserWarning

class TestOrderingActivity : BaseActivity() {

    private lateinit var binding: ActivityTestOrderingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        binding = ActivityTestOrderingBinding.inflate(layoutInflater)

        with(binding) {

            setContentView(root)

            setNavigateUpToolbar(
                primaryToolbar.toolbar,
                R.string.book_free_test,
                upIndicator = R.drawable.ic_arrow_back_white
            )

            orderTest.setUpOpensInBrowserWarning()

            gettingTestedParagraph.setRawText(getString(R.string.test_ordering_getting_tested_description))
        }
        setupListeners()
    }

    private fun setupListeners() {
        binding.orderTest.setOnSingleClickListener {
            startActivityForResult(
                TestOrderingProgressActivity.getIntent(this),
                REQUEST_CODE_ORDER_A_TEST
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ORDER_A_TEST && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    companion object {
        const val REQUEST_CODE_ORDER_A_TEST = 1338

        fun getIntent(context: Context) =
            Intent(context, TestOrderingActivity::class.java)
    }
}
