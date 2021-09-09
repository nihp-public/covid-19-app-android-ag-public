package uk.nhs.nhsx.covid19.android.app.state

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit.DAYS

class IsolationExpirationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val stateMachine = mockk<IsolationStateMachine>(relaxUnitFun = true)

    private val fixedClock = Clock.fixed(Instant.parse("2020-09-01T10:00:00Z"), ZoneOffset.UTC)
    private val isolationHelper = IsolationLogicalHelper(fixedClock)

    private val viewStateObserver = mockk<Observer<IsolationExpirationViewModel.ViewState>>(relaxed = true)

    private val testSubject = IsolationExpirationViewModel(
        stateMachine,
        fixedClock
    )

    private val isolationStateIndexCase = isolationHelper.selfAssessment(
        selfAssessmentDate = isolationStart,
        expiryDate = isolationStart.plus(7, DAYS)
    ).asIsolation()

    private val isolationStateNotInIndexCase = isolationHelper.contactCase(
        exposureDate = isolationStart,
        notificationDate = isolationStart,
        expiryDate = isolationStart.plus(7, DAYS),
    ).asIsolation()

    @Test
    fun `non-parsable isolation expiry date string provided`() = runBlocking {
        testSubject.viewState().observeForever(viewStateObserver)

        testSubject.checkState("non-parsable")

        verify(exactly = 0) { viewStateObserver.onChanged(any()) }
    }

    @Test
    fun `check after now in index case and show temperature notice`() = runBlocking {
        every { stateMachine.readLogicalState() } returns isolationStateIndexCase

        testSubject.viewState().observeForever(viewStateObserver)

        testSubject.checkState("2020-09-02")

        val expired = false
        val expiryDate = LocalDate.parse("2020-09-02")
        val showTemperatureNotice = true

        verify { viewStateObserver.onChanged(IsolationExpirationViewModel.ViewState(expired, expiryDate, showTemperatureNotice)) }
    }

    @Test
    fun `check before now in index case and show temperature notice`() = runBlocking {
        every { stateMachine.readLogicalState() } returns isolationStateIndexCase

        testSubject.viewState().observeForever(viewStateObserver)

        testSubject.checkState("2020-08-31")

        val expired = true
        val expiryDate = LocalDate.parse("2020-08-31")
        val showTemperatureNotice = true

        verify { viewStateObserver.onChanged(IsolationExpirationViewModel.ViewState(expired, expiryDate, showTemperatureNotice)) }
    }

    @Test
    fun `check after now not in index case and don't show temperature notice`() = runBlocking {
        every { stateMachine.readLogicalState() } returns isolationStateNotInIndexCase

        testSubject.viewState().observeForever(viewStateObserver)

        testSubject.checkState("2020-09-02")

        val expired = false
        val expiryDate = LocalDate.parse("2020-09-02")
        val showTemperatureNotice = false

        verify { viewStateObserver.onChanged(IsolationExpirationViewModel.ViewState(expired, expiryDate, showTemperatureNotice)) }
    }

    @Test
    fun `check before now not in index case and don't show temperature notice`() = runBlocking {
        every { stateMachine.readLogicalState() } returns isolationStateNotInIndexCase

        testSubject.viewState().observeForever(viewStateObserver)

        testSubject.checkState("2020-08-31")

        val expired = true
        val expiryDate = LocalDate.parse("2020-08-31")
        val showTemperatureNotice = false

        verify { viewStateObserver.onChanged(IsolationExpirationViewModel.ViewState(expired, expiryDate, showTemperatureNotice)) }
    }

    @Test
    fun `check after now not in isolation and don't show temperature notice`() = runBlocking {
        every { stateMachine.readLogicalState() } returns isolationHelper.neverInIsolation()

        testSubject.viewState().observeForever(viewStateObserver)

        testSubject.checkState("2020-09-02")

        val expired = false
        val expiryDate = LocalDate.parse("2020-09-02")
        val showTemperatureNotice = false

        verify { viewStateObserver.onChanged(IsolationExpirationViewModel.ViewState(expired, expiryDate, showTemperatureNotice)) }
    }

    @Test
    fun `when acknowledging isolation expiration, notify state machine`() {
        testSubject.acknowledgeIsolationExpiration()

        verify { stateMachine.acknowledgeIsolationExpiration() }
    }

    companion object {
        private val isolationStart = LocalDate.parse("2020-08-30")!!
    }
}
