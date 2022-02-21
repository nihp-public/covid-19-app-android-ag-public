package uk.nhs.nhsx.covid19.android.app.testordering.linktestresult

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import uk.nhs.nhsx.covid19.android.app.common.postcode.LocalAuthorityPostCodeProvider
import uk.nhs.nhsx.covid19.android.app.common.postcode.PostCodeDistrict
import uk.nhs.nhsx.covid19.android.app.remote.VirologyTestingApi
import uk.nhs.nhsx.covid19.android.app.remote.data.SupportedCountry.ENGLAND
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyCtaExchangeRequest
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyCtaExchangeResponse
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType.LAB_RESULT
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType.RAPID_RESULT
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType.RAPID_SELF_REPORTED
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestResult
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestResult.NEGATIVE
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestResult.POSITIVE
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestResult.VOID
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.CtaTokenValidator.CtaTokenValidationResult.Failure
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.CtaTokenValidator.CtaTokenValidationResult.Success
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.CtaTokenValidator.CtaTokenValidationResult.UnparsableTestResult
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.CtaTokenValidator.ValidationErrorType.INVALID
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.CtaTokenValidator.ValidationErrorType.NO_CONNECTION
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.CtaTokenValidator.ValidationErrorType.UNEXPECTED
import java.io.IOException
import java.time.Instant
import kotlin.test.assertEquals

class CtaTokenValidatorTest {

    private val virologyTestingApi = mockk<VirologyTestingApi>()
    private val localAuthorityPostCodeProvider = mockk<LocalAuthorityPostCodeProvider>()
    private val crockfordDammValidator = mockk<CrockfordDammValidator>()

    private val testSubject =
        CtaTokenValidator(virologyTestingApi, localAuthorityPostCodeProvider, crockfordDammValidator)

    @Before
    fun setUp() {
        coEvery { localAuthorityPostCodeProvider.getPostCodeDistrict() } returns PostCodeDistrict.ENGLAND
        every { crockfordDammValidator.validate(any()) } returns true
    }

    @Test
    fun `cta token length wrong - 0`() = runBlocking {
        val ctaToken = ""

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(INVALID), result)
    }

    @Test
    fun `cta token length wrong - 1`() = runBlocking {
        val ctaToken = "a"

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(INVALID), result)
    }

    @Test
    fun `cta token length wrong - 7`() = runBlocking {
        val ctaToken = "aaaaaaa"

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(INVALID), result)
    }

    @Test
    fun `cta token length wrong - 9`() = runBlocking {
        val ctaToken = "aaaaaaaaa"

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(INVALID), result)
    }

    @Test
    fun `crockford damm validator returns false`() = runBlocking {
        every { crockfordDammValidator.validate(any()) } returns false

        val invalidCtaToken = "12345678"

        val result = testSubject.validate(invalidCtaToken)

        assertEquals(Failure(INVALID), result)
    }

    @Test
    fun `cta token valid for positive PCR test returns success`() = runBlocking {
        val ctaToken = "12345678"
        val response = setUpTokenResponse(ctaToken, POSITIVE, LAB_RESULT)

        val result = testSubject.validate(ctaToken)

        assertEquals(Success(response.body()!!), result)
    }

    @Test
    fun `cta token valid for negative PCR test returns success`() = runBlocking {
        val ctaToken = "12345678"
        val response = setUpTokenResponse(ctaToken, NEGATIVE, LAB_RESULT)

        val result = testSubject.validate(ctaToken)

        assertEquals(Success(response.body()!!), result)
    }

    @Test
    fun `cta token valid for void PCR test returns success`() = runBlocking {
        val ctaToken = "12345678"
        val response = setUpTokenResponse(ctaToken, VOID, LAB_RESULT)

        val result = testSubject.validate(ctaToken)

        assertEquals(Success(response.body()!!), result)
    }

    @Test
    fun `cta token valid for positive assisted LFD test returns success`() = runBlocking {
        val ctaToken = "12345678"
        val response = setUpTokenResponse(ctaToken, POSITIVE, RAPID_RESULT)

        val result = testSubject.validate(ctaToken)

        assertEquals(Success(response.body()!!), result)
    }

    @Test
    fun `cta token valid for positive unassisted LFD test returns success`() = runBlocking {
        val ctaToken = "12345678"
        val response = setUpTokenResponse(ctaToken, POSITIVE, RAPID_SELF_REPORTED)

        val result = testSubject.validate(ctaToken)

        assertEquals(Success(response.body()!!), result)
    }

    @Test
    fun `cta token valid for negative assisted LFD test results in unexpected error state`() = runBlocking {
        val ctaToken = "12345678"
        setUpTokenResponse(ctaToken, NEGATIVE, RAPID_RESULT)

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(UNEXPECTED), result)
    }

    @Test
    fun `cta token valid for negative unassisted LFD test results in unexpected error state`() = runBlocking {
        val ctaToken = "12345678"
        setUpTokenResponse(ctaToken, NEGATIVE, RAPID_SELF_REPORTED)

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(UNEXPECTED), result)
    }

    @Test
    fun `cta token valid for void assisted LFD test results in unexpected error state`() = runBlocking {
        val ctaToken = "12345678"
        setUpTokenResponse(ctaToken, VOID, RAPID_RESULT)

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(UNEXPECTED), result)
    }

    @Test
    fun `cta token valid for void unassisted LFD test results in unexpected error state`() = runBlocking {
        val ctaToken = "12345678"
        setUpTokenResponse(ctaToken, VOID, RAPID_SELF_REPORTED)

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(UNEXPECTED), result)
    }

    @Test
    fun `cta token validation returns 400 results in invalid code error state`() = runBlocking {
        val ctaToken = "12345678"

        val response = Response.error<VirologyCtaExchangeResponse>(
            400,
            "{}".toResponseBody("application/json".toMediaType())
        )

        coEvery {
            virologyTestingApi.getTestResultForCtaToken(
                VirologyCtaExchangeRequest(ctaToken, ENGLAND)
            )
        } returns response

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(INVALID), result)
    }

    @Test
    fun `cta token validation returns 404 results in invalid code error state`() = runBlocking {
        val ctaToken = "12345678"

        val response = Response.error<VirologyCtaExchangeResponse>(
            404,
            "{}".toResponseBody("application/json".toMediaType())
        )

        coEvery {
            virologyTestingApi.getTestResultForCtaToken(
                VirologyCtaExchangeRequest(ctaToken, ENGLAND)
            )
        } returns response

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(INVALID), result)
    }

    @Test
    fun `cta token validation returns 500 results in unexpected error state`() = runBlocking {
        val ctaToken = "12345678"

        val response = Response.error<VirologyCtaExchangeResponse>(
            500,
            "{}".toResponseBody("application/json".toMediaType())
        )

        coEvery {
            virologyTestingApi.getTestResultForCtaToken(
                VirologyCtaExchangeRequest(ctaToken, ENGLAND)
            )
        } returns response

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(UNEXPECTED), result)
    }

    @Test
    fun `cta token validation throws IOException results in no connection error state`() =
        runBlocking {
            val ctaToken = "12345678"

            coEvery {
                virologyTestingApi.getTestResultForCtaToken(
                    VirologyCtaExchangeRequest(ctaToken, ENGLAND)
                )
            } throws IOException()

            val result = testSubject.validate(ctaToken)

            assertEquals(Failure(NO_CONNECTION), result)
        }

    @Test
    fun `cta token validation throws JsonDataException results in UnknownTestResult`() =
        runBlocking {
            val ctaToken = "12345678"

            coEvery {
                virologyTestingApi.getTestResultForCtaToken(
                    VirologyCtaExchangeRequest(ctaToken, ENGLAND)
                )
            } throws JsonDataException()

            val result = testSubject.validate(ctaToken)

            assertEquals(UnparsableTestResult, result)
        }

    @Test
    fun `cta token validation throws JsonEncodingException results in UnknownTestResult`() =
        runBlocking {
            val ctaToken = "12345678"

            coEvery {
                virologyTestingApi.getTestResultForCtaToken(
                    VirologyCtaExchangeRequest(ctaToken, ENGLAND)
                )
            } throws JsonEncodingException(null)

            val result = testSubject.validate(ctaToken)

            assertEquals(UnparsableTestResult, result)
        }

    @Test
    fun `cta token validation throws other exception results in unexpected error state`() =
        runBlocking {
            val ctaToken = "12345678"

            coEvery {
                virologyTestingApi.getTestResultForCtaToken(
                    VirologyCtaExchangeRequest(ctaToken, ENGLAND)
                )
            } throws Exception()

            val result = testSubject.validate(ctaToken)

            assertEquals(Failure(UNEXPECTED), result)
        }

    @Test
    fun `no supported country results in unexpected error state`() =
        runBlocking {
            val ctaToken = "12345678"

            coEvery { localAuthorityPostCodeProvider.getPostCodeDistrict() } returns null

            val result = testSubject.validate(ctaToken)

            assertEquals(Failure(UNEXPECTED), result)
        }

    @Test
    fun `negative indicative test is not permitted`() = runBlocking {
        val ctaToken = "12345678"
        setUpTokenResponse(ctaToken, NEGATIVE, LAB_RESULT, requiresConfirmatoryTest = true)

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(UNEXPECTED), result)
    }

    @Test
    fun `void indicative test is not permitted`() = runBlocking {
        val ctaToken = "12345678"
        setUpTokenResponse(ctaToken, VOID, LAB_RESULT, requiresConfirmatoryTest = true)

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(UNEXPECTED), result)
    }

    @Test
    fun `confirmatory test must not have confirmatoryDayLimit`() = runBlocking {
        val ctaToken = "12345678"
        setUpTokenResponse(ctaToken, POSITIVE, LAB_RESULT, confirmatoryDayLimit = 1)

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(UNEXPECTED), result)
    }

    @Test
    fun `follow up test should not be offered when confirmatory test is not required`() = runBlocking {
        val ctaToken = "12345678"
        setUpTokenResponse(ctaToken, POSITIVE, RAPID_RESULT, requiresConfirmatoryTest = false, shouldOfferFollowUpTest = true)

        val result = testSubject.validate(ctaToken)

        assertEquals(Failure(UNEXPECTED), result)
    }

    private fun setUpTokenResponse(
        ctaToken: String,
        testResult: VirologyTestResult,
        testKitType: VirologyTestKitType,
        requiresConfirmatoryTest: Boolean = false,
        shouldOfferFollowUpTest: Boolean = requiresConfirmatoryTest,
        confirmatoryDayLimit: Int? = null
    ): Response<VirologyCtaExchangeResponse> {
        val response = Response.success(
            VirologyCtaExchangeResponse(
                "submissionToken",
                Instant.now(),
                testResult,
                testKitType,
                diagnosisKeySubmissionSupported = true,
                requiresConfirmatoryTest = requiresConfirmatoryTest,
                shouldOfferFollowUpTest = shouldOfferFollowUpTest,
                confirmatoryDayLimit = confirmatoryDayLimit
            )
        )

        coEvery {
            virologyTestingApi.getTestResultForCtaToken(
                VirologyCtaExchangeRequest(ctaToken, ENGLAND)
            )
        } returns response

        return response
    }
}
