package uk.nhs.nhsx.covid19.android.app.di.module

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import uk.nhs.nhsx.covid19.android.app.di.module.NetworkModule.Companion.API_REMOTE
import uk.nhs.nhsx.covid19.android.app.di.module.NetworkModule.Companion.DISTRIBUTION_REMOTE
import uk.nhs.nhsx.covid19.android.app.remote.AnalyticsApi
import uk.nhs.nhsx.covid19.android.app.remote.AppAvailabilityApi
import uk.nhs.nhsx.covid19.android.app.remote.EmptyApi
import uk.nhs.nhsx.covid19.android.app.remote.EpidemiologyDataApi
import uk.nhs.nhsx.covid19.android.app.remote.ExposureCircuitBreakerApi
import uk.nhs.nhsx.covid19.android.app.remote.ExposureConfigurationApi
import uk.nhs.nhsx.covid19.android.app.remote.IsolationConfigurationApi
import uk.nhs.nhsx.covid19.android.app.remote.IsolationPaymentApi
import uk.nhs.nhsx.covid19.android.app.remote.KeysDistributionApi
import uk.nhs.nhsx.covid19.android.app.remote.KeysSubmissionApi
import uk.nhs.nhsx.covid19.android.app.remote.LocalMessagesApi
import uk.nhs.nhsx.covid19.android.app.remote.LocalStatsApi
import uk.nhs.nhsx.covid19.android.app.remote.QuestionnaireApi
import uk.nhs.nhsx.covid19.android.app.remote.RemoteServiceExceptionCrashReportSubmissionApi
import uk.nhs.nhsx.covid19.android.app.remote.RiskyPostDistrictsApi
import uk.nhs.nhsx.covid19.android.app.remote.RiskyVenueConfigurationApi
import uk.nhs.nhsx.covid19.android.app.remote.RiskyVenuesApi
import uk.nhs.nhsx.covid19.android.app.remote.RiskyVenuesCircuitBreakerApi
import uk.nhs.nhsx.covid19.android.app.remote.VirologyTestingApi
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApiModule {
    @Provides
    @Singleton
    fun provideKeysSubmissionApi(@Named(API_REMOTE) retrofit: Retrofit): KeysSubmissionApi {
        return retrofit.create(KeysSubmissionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideKeysDistributionApi(@Named(DISTRIBUTION_REMOTE) retrofit: Retrofit): KeysDistributionApi {
        return retrofit.create(KeysDistributionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRiskyPostDistrictsApi(@Named(DISTRIBUTION_REMOTE) retrofit: Retrofit): RiskyPostDistrictsApi {
        return retrofit.create(RiskyPostDistrictsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRiskyVenuesApi(@Named(DISTRIBUTION_REMOTE) retrofit: Retrofit): RiskyVenuesApi =
        retrofit.create(RiskyVenuesApi::class.java)

    @Provides
    @Singleton
    fun provideQuestionnaireApi(@Named(DISTRIBUTION_REMOTE) retrofit: Retrofit): QuestionnaireApi =
        retrofit.create(QuestionnaireApi::class.java)

    @Provides
    @Singleton
    fun provideVirologyTestingApi(@Named(API_REMOTE) retrofit: Retrofit): VirologyTestingApi =
        retrofit.create(VirologyTestingApi::class.java)

    @Provides
    @Singleton
    fun provideExposureCircuitBreaker(@Named(API_REMOTE) retrofit: Retrofit): ExposureCircuitBreakerApi =
        retrofit.create(ExposureCircuitBreakerApi::class.java)

    @Provides
    @Singleton
    fun provideExposureConfigurationApi(@Named(DISTRIBUTION_REMOTE) retrofit: Retrofit): ExposureConfigurationApi =
        retrofit.create(ExposureConfigurationApi::class.java)

    @Provides
    @Singleton
    fun provideRiskyVenuesCircuitBreaker(@Named(API_REMOTE) retrofit: Retrofit): RiskyVenuesCircuitBreakerApi =
        retrofit.create(RiskyVenuesCircuitBreakerApi::class.java)

    @Provides
    @Singleton
    fun provideIsolationConfigurationApi(@Named(DISTRIBUTION_REMOTE) retrofit: Retrofit): IsolationConfigurationApi =
        retrofit.create(IsolationConfigurationApi::class.java)

    @Provides
    @Singleton
    fun provideAppAvailabilityApi(@Named(DISTRIBUTION_REMOTE) retrofit: Retrofit): AppAvailabilityApi =
        retrofit.create(AppAvailabilityApi::class.java)

    @Provides
    @Singleton
    fun provideAnalyticsApi(@Named(API_REMOTE) retrofit: Retrofit): AnalyticsApi =
        retrofit.create(AnalyticsApi::class.java)

    @Provides
    @Singleton
    fun provideEpidemiologyDataApi(@Named(API_REMOTE) retrofit: Retrofit): EpidemiologyDataApi =
        retrofit.create(EpidemiologyDataApi::class.java)

    @Provides
    @Singleton
    fun provideEmptyApi(@Named(API_REMOTE) retrofit: Retrofit): EmptyApi =
        retrofit.create(EmptyApi::class.java)

    @Provides
    @Singleton
    fun provideIsolationPaymentApi(@Named(API_REMOTE) retrofit: Retrofit): IsolationPaymentApi =
        retrofit.create(IsolationPaymentApi::class.java)

    @Provides
    @Singleton
    fun provideRiskyVenueConfigurationApi(@Named(DISTRIBUTION_REMOTE) retrofit: Retrofit): RiskyVenueConfigurationApi =
        retrofit.create(RiskyVenueConfigurationApi::class.java)

    @Provides
    @Singleton
    fun provideRemoteServiceExceptionCrashReportSubmissionApi(@Named(API_REMOTE) retrofit: Retrofit): RemoteServiceExceptionCrashReportSubmissionApi =
        retrofit.create(RemoteServiceExceptionCrashReportSubmissionApi::class.java)

    @Provides
    @Singleton
    fun provideLocalMessagesDistributionApi(@Named(DISTRIBUTION_REMOTE) retrofit: Retrofit): LocalMessagesApi =
        retrofit.create(LocalMessagesApi::class.java)

    @Provides
    @Singleton
    fun provideLocalStatsDistributionApi(@Named(DISTRIBUTION_REMOTE) retrofit: Retrofit): LocalStatsApi =
        retrofit.create(LocalStatsApi::class.java)
}
