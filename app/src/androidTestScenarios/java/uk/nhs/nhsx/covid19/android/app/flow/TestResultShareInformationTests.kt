package uk.nhs.nhsx.covid19.android.app.flow

import com.jeroenmols.featureflag.framework.FeatureFlag.SELF_REPORTING
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import uk.nhs.nhsx.covid19.android.app.exposure.MockExposureNotificationApi.Result.ResolutionRequired
import uk.nhs.nhsx.covid19.android.app.exposure.MockExposureNotificationApi.Result.Success
import uk.nhs.nhsx.covid19.android.app.exposure.createExposureNotificationResolutionPendingIntent
import uk.nhs.nhsx.covid19.android.app.remote.MockVirologyTestingApi
import uk.nhs.nhsx.covid19.android.app.status.StatusActivity
import uk.nhs.nhsx.covid19.android.app.testhelpers.TestApplicationContext
import uk.nhs.nhsx.covid19.android.app.testhelpers.base.EspressoTest
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.LinkTestResultRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.LinkTestResultSymptomsRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.ShareKeysInformationRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.ShareKeysResultRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.StatusRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.TestResultRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.runWithFeature

class TestResultShareInformationTests : EspressoTest() {

    private val statusRobot = StatusRobot()
    private val linkTestResultRobot = LinkTestResultRobot()
    private val linkTestResultSymptomsRobot = LinkTestResultSymptomsRobot()
    private val testResultRobot = TestResultRobot(testAppContext.app)
    private val shareKeysInformationRobot = ShareKeysInformationRobot()
    private val shareKeysResultRobot = ShareKeysResultRobot()

    @Before
    fun setUp() {
        testAppContext.setLocalAuthority(TestApplicationContext.WELSH_LOCAL_AUTHORITY)
    }

    @Test
    fun linkTestResult_shareKeys() = runWithFeature(SELF_REPORTING, enabled = false) {
        val resolutionIntent = createExposureNotificationResolutionPendingIntent(testAppContext.app, successful = true)
        testAppContext.getExposureNotificationApi().temporaryExposureKeyHistoryResult =
            ResolutionRequired(resolutionIntent, Success())

        startTestActivity<StatusActivity>()

        statusRobot.checkActivityIsDisplayed()

        statusRobot.clickLinkTestResult()

        linkTestResultRobot.checkActivityIsDisplayed()

        linkTestResultRobot.enterCtaToken(MockVirologyTestingApi.POSITIVE_PCR_TOKEN)

        linkTestResultRobot.clickContinue()

        waitFor { linkTestResultSymptomsRobot.checkActivityIsDisplayed() }

        linkTestResultSymptomsRobot.clickNo()

        waitFor {
            testResultRobot.checkActivityDisplaysPositiveWillBeInIsolation(remainingDaysInIsolation = 3)
        }

        testResultRobot.clickIsolationActionButton()

        shareKeysInformationRobot.checkActivityIsDisplayed()

        shareKeysInformationRobot.clickContinueButton()

        waitFor { shareKeysResultRobot.checkActivityIsDisplayed() }

        shareKeysResultRobot.clickActionButton()

        waitFor { statusRobot.checkActivityIsDisplayed() }
    }

    @Test
    fun linkTestResult_doNotShareKeys() = runWithFeature(SELF_REPORTING, enabled = false) {
        runBlocking {
            val resolutionIntent = createExposureNotificationResolutionPendingIntent(testAppContext.app, successful = false)
            testAppContext.getExposureNotificationApi().temporaryExposureKeyHistoryResult =
                ResolutionRequired(resolutionIntent, Success())

            startTestActivity<StatusActivity>()

            statusRobot.checkActivityIsDisplayed()

            statusRobot.clickLinkTestResult()

            linkTestResultRobot.checkActivityIsDisplayed()

            linkTestResultRobot.enterCtaToken(MockVirologyTestingApi.POSITIVE_PCR_TOKEN)

            linkTestResultRobot.clickContinue()

            waitFor { linkTestResultSymptomsRobot.checkActivityIsDisplayed() }

            linkTestResultSymptomsRobot.clickNo()

            waitFor {
                testResultRobot.checkActivityDisplaysPositiveWillBeInIsolation(remainingDaysInIsolation = 3)
            }

            testResultRobot.clickIsolationActionButton()

            shareKeysInformationRobot.checkActivityIsDisplayed()

            shareKeysInformationRobot.clickContinueButton()

            waitFor { statusRobot.checkActivityIsDisplayed() }
        }
    }
}
