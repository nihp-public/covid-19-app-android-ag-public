package uk.nhs.nhsx.covid19.android.app.qrcode

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uk.nhs.nhsx.covid19.android.app.qrcode.QrCodeScanResult.Success
import uk.nhs.nhsx.covid19.android.app.report.Reported
import uk.nhs.nhsx.covid19.android.app.report.Reporter.Kind.FLOW
import uk.nhs.nhsx.covid19.android.app.report.config.TestConfiguration
import uk.nhs.nhsx.covid19.android.app.report.reporter
import uk.nhs.nhsx.covid19.android.app.testhelpers.TestApplicationContext
import uk.nhs.nhsx.covid19.android.app.testhelpers.base.EspressoTest
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.QrCodeScanResultRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.QrScannerRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.VenueAlertInformRobot

@RunWith(Parameterized::class)
class QrCodeScanScenarioTest(override val configuration: TestConfiguration) : EspressoTest() {

    @get:Rule
    var cameraPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(CAMERA)

    private val qrScannerRobot = QrScannerRobot()
    private val qrCodeScanResultRobot = QrCodeScanResultRobot()
    private val venueAlertInformRobot = VenueAlertInformRobot()

    @Before
    fun setUp() {
        testAppContext.setLocalAuthority(TestApplicationContext.ENGLISH_LOCAL_AUTHORITY)
    }

    @After
    fun tearDown() {
        testAppContext.permissionsManager.clear()
    }

    @Test
    @Reported
    fun checkInWithValidQrCodeAndCameraPermissionEnabled_shouldReceiveRiskyVenueWarning() =
        reporter(
            scenario = "Venue check-in",
            title = "Happy path",
            description = "The user successfully scans an official NHS QR code and later receives warning about visited venue being risky",
            kind = FLOW
        ) {
            testAppContext.permissionsManager.setResponseForPermissionRequest(
                CAMERA,
                PackageManager.PERMISSION_GRANTED
            )

            startTestActivity<QrScannerActivity>()

            qrScannerRobot.checkActivityIsDisplayed()

            step(
                stepName = "Scanning QR code",
                stepDescription = "User is shown a screen to scan a QR code"
            )

            runBlocking {
                testAppContext.getVisitedVenuesStorage().finishLastVisitAndAddNewVenue(
                    Venue("ABCD1234", "ABCD1234")
                )
            }

            startTestActivity<QrCodeScanResultActivity> {
                putExtra(QrCodeScanResultActivity.SCAN_RESULT, Success("ABCD1234"))
            }

            waitFor { qrCodeScanResultRobot.checkAnimationIconIsDisplayed() }

            qrCodeScanResultRobot.checkSuccessTitleAndVenueIsDisplayed("ABCD1234")

            step(
                stepName = "Successful scan",
                stepDescription = "User successfully checks in using an official NHS QR code. They tap 'Back to home'."
            )

            qrCodeScanResultRobot.clickBackToHomeButton()

            runBlocking {
                testAppContext.getDownloadAndProcessRiskyVenues().invoke()
            }

            waitFor { venueAlertInformRobot.checkVenueTitleIsDisplayed() }

            step(
                stepName = "Risky venue alert",
                stepDescription = "User is presented a screen that informs them they have recently visited a risky venue"
            )
        }

    @Test
    fun checkInWithValidQrCodeAndCameraPermissionDisabled_shouldRequestPermission_onPermissionGranted_shouldReceiveRiskyVenueWarning() {
        startTestActivity<QrScannerActivity>()

        qrScannerRobot.checkActivityIsDisplayed()

        runBlocking {
            testAppContext.getVisitedVenuesStorage().finishLastVisitAndAddNewVenue(
                Venue("ABCD1234", "ABCD1234")
            )
        }

        startTestActivity<QrCodeScanResultActivity> {
            putExtra(QrCodeScanResultActivity.SCAN_RESULT, Success("ABCD1234"))
        }

        waitFor { qrCodeScanResultRobot.checkAnimationIconIsDisplayed() }

        qrCodeScanResultRobot.checkSuccessTitleAndVenueIsDisplayed("ABCD1234")

        qrCodeScanResultRobot.clickBackToHomeButton()

        runBlocking {
            testAppContext.getDownloadAndProcessRiskyVenues().invoke()
        }

        waitFor { venueAlertInformRobot.checkVenueTitleIsDisplayed() }
    }

    @Test
    fun checkInWithValidQrCodeAndCameraPermissionDisabled_shouldRequestPermission_onPermissionDenied_shouldShowPermissionDenied() {
        testAppContext.permissionsManager.setResponseForPermissionRequest(
            CAMERA,
            PackageManager.PERMISSION_DENIED
        )

        startTestActivity<QrScannerActivity>()

        waitFor { qrCodeScanResultRobot.checkCameraIconIsDisplayed() }

        qrCodeScanResultRobot.checkPermissionDeniedTitleIsDisplayed()
    }
}
