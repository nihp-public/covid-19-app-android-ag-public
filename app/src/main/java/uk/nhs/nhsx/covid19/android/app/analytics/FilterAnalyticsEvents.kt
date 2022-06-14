package uk.nhs.nhsx.covid19.android.app.analytics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.nhs.nhsx.covid19.android.app.remote.data.Metrics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterAnalyticsEvents @Inject constructor(
    private val analyticsFilterProvider: AnalyticsFilterProvider
) {
    suspend operator fun invoke(metrics: Metrics): Metrics = withContext(Dispatchers.IO) {
        val analyticsFilter = analyticsFilterProvider.invoke()
        return@withContext filterAnalyticsMetrics(metrics, analyticsFilter)
    }

    private fun filterAnalyticsMetrics(metrics: Metrics, analyticsFilter: AnalyticsFilter): Metrics {
        with(analyticsFilter) {
            if (shouldFilterRiskyContactInfo) {
                metrics.acknowledgedStartOfIsolationDueToRiskyContact = null
                metrics.isIsolatingForHadRiskyContactBackgroundTick = null
            }

            if (shouldFilterSelfIsolation) {
                metrics.didAccessSelfIsolationNoteLink = null
                metrics.receivedActiveIpcToken = null
                metrics.haveActiveIpcTokenBackgroundTick = null
                metrics.selectedIsolationPaymentsButton = null
                metrics.launchedIsolationPaymentsApplication = null
            }

            if (shouldFilterVenueCheckIn) {
                metrics.receivedRiskyVenueM2Warning = null
                metrics.hasReceivedRiskyVenueM2WarningBackgroundTick = null
                metrics.didAccessRiskyVenueM2Notification = null
                metrics.selectedTakeTestM2Journey = null
                metrics.selectedTakeTestLaterM2Journey = null
                metrics.selectedHasSymptomsM2Journey = null
                metrics.selectedHasNoSymptomsM2Journey = null
                metrics.selectedLFDTestOrderingM2Journey = null
                metrics.selectedHasLFDTestM2Journey = null
                metrics.receivedRiskyVenueM1Warning = null
            }

            enabledCustomAnalyticsFilters.forEach { _ ->
                // Handle any additional custom metrics filtering here
            }
        }
        return metrics
    }
}

data class AnalyticsFilter(
    val shouldFilterVenueCheckIn: Boolean,
    val shouldFilterSelfIsolation: Boolean,
    val shouldFilterRiskyContactInfo: Boolean,
    val enabledCustomAnalyticsFilters: List<CustomAnalyticsFilter>
)

enum class CustomAnalyticsFilter