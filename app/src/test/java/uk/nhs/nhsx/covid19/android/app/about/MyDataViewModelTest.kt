package uk.nhs.nhsx.covid19.android.app.about

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.jeroenmols.featureflag.framework.FeatureFlag.DAILY_CONTACT_TESTING
import com.jeroenmols.featureflag.framework.FeatureFlagTestHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.nhs.nhsx.covid19.android.app.about.BaseMyDataViewModel.IsolationState
import uk.nhs.nhsx.covid19.android.app.about.BaseMyDataViewModel.MyDataState
import uk.nhs.nhsx.covid19.android.app.qrcode.riskyvenues.LastVisitedBookTestTypeVenueDate
import uk.nhs.nhsx.covid19.android.app.qrcode.riskyvenues.LastVisitedBookTestTypeVenueDateProvider
import uk.nhs.nhsx.covid19.android.app.remote.data.DurationDays
import uk.nhs.nhsx.covid19.android.app.remote.data.RiskyVenueConfigurationDurationDays
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType.LAB_RESULT
import uk.nhs.nhsx.covid19.android.app.state.IsolationStateMachine
import uk.nhs.nhsx.covid19.android.app.state.State.Default
import uk.nhs.nhsx.covid19.android.app.state.State.Isolation
import uk.nhs.nhsx.covid19.android.app.state.State.Isolation.ContactCase
import uk.nhs.nhsx.covid19.android.app.testordering.AcknowledgedTestResult
import uk.nhs.nhsx.covid19.android.app.testordering.RelevantTestResultProvider
import uk.nhs.nhsx.covid19.android.app.testordering.RelevantVirologyTestResult.POSITIVE
import java.time.Instant
import java.time.LocalDate

class MyDataViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val stateMachine = mockk<IsolationStateMachine>(relaxed = true)
    private val relevantTestResultProvider = mockk<RelevantTestResultProvider>(relaxed = true)
    private val lastVisitedBookTestTypeVenueDateProvider = mockk<LastVisitedBookTestTypeVenueDateProvider>(relaxUnitFun = true)

    private val testSubject = MyDataViewModel(
        stateMachine,
        relevantTestResultProvider,
        lastVisitedBookTestTypeVenueDateProvider
    )

    private val userDataStateObserver = mockk<Observer<MyDataState>>(relaxed = true)
    private val venueVisitsEditModeChangedObserver = mockk<Observer<Boolean>>(relaxed = true)
    private val allUserDataDeletedObserver = mockk<Observer<Unit>>(relaxed = true)

    @Before
    fun setUp() {
        FeatureFlagTestHelper.enableFeatureFlag(DAILY_CONTACT_TESTING)

        testSubject.myDataState().observeForever(userDataStateObserver)

        every { relevantTestResultProvider.testResult } returns acknowledgedTestResult
        every { lastVisitedBookTestTypeVenueDateProvider.lastVisitedVenue } returns LastVisitedBookTestTypeVenueDate(
            lastRiskyVenueVisit,
            RiskyVenueConfigurationDurationDays(optionToBookATest = 10)
        )
    }

    @After
    fun tearDown() {
        FeatureFlagTestHelper.clearFeatureFlags()
    }

    @Test
    fun `onResume triggers view state emission`() = runBlocking {
        every { stateMachine.readState() } returns contactCaseOnlyIsolation

        testSubject.onResume()

        verify { userDataStateObserver.onChanged(expectedInitialUserDataState) }
        verify(exactly = 0) { venueVisitsEditModeChangedObserver.onChanged(any()) }
        verify(exactly = 0) { allUserDataDeletedObserver.onChanged(any()) }
    }

    @Test
    fun `onResume with no changes to view state does not trigger view state emission`() = runBlocking {
        every { stateMachine.readState() } returns contactCaseOnlyIsolation

        testSubject.onResume()
        testSubject.onResume()

        verify(exactly = 1) { userDataStateObserver.onChanged(any()) }
        verify(exactly = 0) { venueVisitsEditModeChangedObserver.onChanged(any()) }
        verify(exactly = 0) { allUserDataDeletedObserver.onChanged(any()) }
    }

    @Test
    fun `loading user data only returns main post code when local authority is not stored`() {
        every { stateMachine.readState() } returns contactCaseOnlyIsolation

        testSubject.onResume()

        verify(exactly = 0) { venueVisitsEditModeChangedObserver.onChanged(any()) }
        verify(exactly = 0) { allUserDataDeletedObserver.onChanged(any()) }
    }

    @Test
    fun `loading user data returns exposure notification details and dailyContactTestingOptInDate when previously in contact case`() {
        every { stateMachine.readState() } returns Default(previousIsolation = contactCaseOnlyIsolation)

        testSubject.onResume()

        verify {
            userDataStateObserver.onChanged(
                expectedInitialUserDataState.copy(
                    isolationState = IsolationState(
                        contactCaseEncounterDate = contactCaseEncounterDate,
                        contactCaseNotificationDate = contactCaseNotificationDate,
                        dailyContactTestingOptInDate = dailyContactTestingOptInDate
                    )
                )
            )
        }
    }

    @Test
    fun `loading user data doesn't return exposure notification details and dailyContactTestingOptInDate when previously in contact case`() {
        every { stateMachine.readState() } returns Default()

        testSubject.onResume()

        verify {
            userDataStateObserver.onChanged(expectedInitialUserDataState.copy(isolationState = null))
        }
    }

    private val lastRiskyVenueVisit = LocalDate.of(2020, 8, 12)

    private val acknowledgedTestResult = AcknowledgedTestResult(
        diagnosisKeySubmissionToken = "token",
        testEndDate = Instant.now(),
        testResult = POSITIVE,
        acknowledgedDate = Instant.now(),
        testKitType = LAB_RESULT,
        requiresConfirmatoryTest = false,
        confirmedDate = null
    )

    private val contactCaseEncounterDate = Instant.parse("2020-05-19T12:00:00Z")

    private val contactCaseNotificationDate = Instant.parse("2020-05-20T12:00:00Z")

    private val dailyContactTestingOptInDate = LocalDate.now().plusDays(5)

    private val contactCaseOnlyIsolation = Isolation(
        isolationStart = Instant.now(),
        isolationConfiguration = DurationDays(),
        contactCase = ContactCase(
            startDate = contactCaseEncounterDate,
            notificationDate = contactCaseNotificationDate,
            expiryDate = LocalDate.now().plusDays(5),
            dailyContactTestingOptInDate = dailyContactTestingOptInDate
        )
    )

    private val expectedInitialUserDataState = MyDataState(
        isolationState = IsolationState(
            lastDayOfIsolation = contactCaseOnlyIsolation.lastDayOfIsolation,
            contactCaseEncounterDate = contactCaseEncounterDate,
            contactCaseNotificationDate = contactCaseNotificationDate,
            indexCaseSymptomOnsetDate = contactCaseOnlyIsolation.indexCase?.symptomsOnsetDate,
            dailyContactTestingOptInDate = dailyContactTestingOptInDate
        ),
        lastRiskyVenueVisitDate = lastRiskyVenueVisit,
        acknowledgedTestResult = acknowledgedTestResult
    )
}