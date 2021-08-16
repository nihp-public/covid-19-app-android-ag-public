package uk.nhs.nhsx.covid19.android.app.about.mydata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uk.nhs.nhsx.covid19.android.app.testordering.AcknowledgedTestResult
import java.time.LocalDate

abstract class BaseMyDataViewModel : ViewModel() {
    protected val myDataStateLiveData = MutableLiveData<MyDataState>()
    fun myDataState(): LiveData<MyDataState> = myDataStateLiveData

    abstract fun onResume()

    protected abstract fun getIsolationState(): IsolationViewState?
    protected abstract fun getLastRiskyVenueVisitDate(): LocalDate?

    data class MyDataState(
        val isolationState: IsolationViewState?,
        val lastRiskyVenueVisitDate: LocalDate?,
        val acknowledgedTestResult: AcknowledgedTestResult?,
    )

    data class IsolationViewState(
        val lastDayOfIsolation: LocalDate? = null,
        val contactCaseEncounterDate: LocalDate? = null,
        val contactCaseNotificationDate: LocalDate? = null,
        val indexCaseSymptomOnsetDate: LocalDate? = null,
        val optOutOfContactIsolationDate: LocalDate? = null,
    )
}
