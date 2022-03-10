@file:Suppress("DEPRECATION")

package uk.nhs.nhsx.covid19.android.app.state

import timber.log.Timber
import uk.nhs.nhsx.covid19.android.app.state.IsolationState.Contact
import uk.nhs.nhsx.covid19.android.app.state.IsolationState.SelfAssessment
import uk.nhs.nhsx.covid19.android.app.state.IsolationState.OptOutOfContactIsolation
import uk.nhs.nhsx.covid19.android.app.state.State4_9.Default4_9
import uk.nhs.nhsx.covid19.android.app.state.State4_9.Isolation4_9
import uk.nhs.nhsx.covid19.android.app.state.State4_9.Isolation4_9.ContactCase4_9
import uk.nhs.nhsx.covid19.android.app.state.State4_9.Isolation4_9.IndexCase4_9
import uk.nhs.nhsx.covid19.android.app.state.StateStorage4_9.Companion.assumedDaysFromOnsetToSelfAssessment4_9
import uk.nhs.nhsx.covid19.android.app.testordering.AcknowledgedTestResult
import uk.nhs.nhsx.covid19.android.app.testordering.AcknowledgedTestResult4_9
import uk.nhs.nhsx.covid19.android.app.testordering.RelevantTestResultProvider
import uk.nhs.nhsx.covid19.android.app.testordering.RelevantVirologyTestResult.NEGATIVE
import uk.nhs.nhsx.covid19.android.app.testordering.RelevantVirologyTestResult.POSITIVE
import uk.nhs.nhsx.covid19.android.app.util.toLocalDate
import java.time.Clock
import javax.inject.Inject

class MigrateIsolationState @Inject constructor(
    private val stateStorage: StateStorage,
    private val stateStorage4_9: StateStorage4_9,
    private val relevantTestResultProvider: RelevantTestResultProvider,
    private val isolationConfigurationProvider: IsolationConfigurationProvider,
    private val migrateTestResults: MigrateTestResults,
    private val createIsolationConfiguration: CreateIsolationConfiguration,
    private val clock: Clock
) {

    private val lock = Object()

    operator fun invoke() = synchronized(lock) {
        val oldIsolationState = stateStorage4_9.state
        // If oldIsolationState is null the migration has already taken place
        if (oldIsolationState != null) {
            migrateTestResults()
            migrateState(oldIsolationState)
        }
    }

    private fun migrateState(oldIsolationState: State4_9) {
        val relevantTestResult = relevantTestResultProvider.testResult?.toAcknowledgedTestResult()
        val isolationState = oldIsolationState.toIsolationState(relevantTestResult)

        stateStorage.state = isolationState

        relevantTestResultProvider.clear()
        stateStorage4_9.clear()
    }

    private fun AcknowledgedTestResult4_9.toAcknowledgedTestResult(): AcknowledgedTestResult =
        AcknowledgedTestResult(
            testEndDate = testEndDate.toLocalDate(clock.zone),
            testResult,
            testKitType,
            acknowledgedDate = acknowledgedDate.toLocalDate(clock.zone),
            requiresConfirmatoryTest,
            confirmedDate = confirmedDate?.toLocalDate(clock.zone)
        )

    private fun State4_9.toIsolationState(testResult: AcknowledgedTestResult?): IsolationState =
        when (this) {
            is Default4_9 ->
                this.previousIsolation?.toIsolationState(testResult, expirationAcknowledged = true)
                    ?: createNeverIsolating(testResult)
            is Isolation4_9 ->
                this.toIsolationState(testResult, expirationAcknowledged = false)
        }

    private fun createNeverIsolating(testResult: AcknowledgedTestResult?): IsolationState =
        IsolationState(
            isolationConfiguration = createIsolationConfiguration(isolationConfigurationProvider.durationDays),
            testResult = handleNoIndexCase(testResult)
        )

    private fun Isolation4_9.toIsolationState(
        testResult: AcknowledgedTestResult?,
        expirationAcknowledged: Boolean
    ): IsolationState =
        IsolationState(
            isolationConfiguration = isolationConfiguration,
            selfAssessment = indexCase?.toSelfAssessment(testResult),
            testResult = testResult,
            contact = contactCase?.toContactCase(),
            hasAcknowledgedEndOfIsolation = expirationAcknowledged
        )

    private fun handleNoIndexCase(testResult: AcknowledgedTestResult?): AcknowledgedTestResult? =
        when {
            testResult == null -> null
            testResult.testResult == NEGATIVE -> testResult
            else -> {
                if (testResult.testResult == POSITIVE) {
                    Timber.e("Found a $POSITIVE test result but no index case. This is not possible. Falling back to null index case => test information will be lost")
                } else {
                    Timber.e("Found an unsupported test result (${testResult.testResult}). Falling back to null index case => test information will be lost")
                }
                null
            }
        }

    private fun IndexCase4_9.toSelfAssessment(testResult: AcknowledgedTestResult?): SelfAssessment? =
        if (!selfAssessment && testResult != null && testResult.testResult == POSITIVE) {
            // The index case is entirely due to a positive test; no self-assessment
            null
        } else {
            if (!selfAssessment) {
                Timber.e("An index case was found but selfAssessment is false and there is no positive test result. This is not be possible. Falling back to self-assessment index case")
            }

            // We're storing more specific information now that we cannot deduce from the old information.
            // We'll assume the user selected "cannot remember symptoms onset date" when performing the self-assessment
            SelfAssessment(
                selfAssessmentDate = symptomsOnsetDate.plusDays(assumedDaysFromOnsetToSelfAssessment4_9),
                onsetDate = null
            )
        }

    private fun ContactCase4_9.toContactCase(): Contact =
        Contact(
            exposureDate = startDate.toLocalDate(clock.zone),
            notificationDate = notificationDate?.toLocalDate(clock.zone)
                // Fall back to exposure date if notification date is not available
                ?: startDate.toLocalDate(clock.zone),
            optOutOfContactIsolation = dailyContactTestingOptInDate?.let { OptOutOfContactIsolation(it) },
        )
}
