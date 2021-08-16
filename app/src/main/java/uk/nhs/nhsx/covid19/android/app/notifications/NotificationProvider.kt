package uk.nhs.nhsx.covid19.android.app.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import uk.nhs.nhsx.covid19.android.app.MainActivity
import uk.nhs.nhsx.covid19.android.app.R
import uk.nhs.nhsx.covid19.android.app.availability.AppAvailabilityActivity
import uk.nhs.nhsx.covid19.android.app.availability.UpdateRecommendedActivity
import uk.nhs.nhsx.covid19.android.app.notifications.NotificationProvider.ContactTracingHubAction.NAVIGATE_AND_TURN_ON
import uk.nhs.nhsx.covid19.android.app.notifications.NotificationProvider.ContactTracingHubAction.ONLY_NAVIGATE
import uk.nhs.nhsx.covid19.android.app.remote.data.RiskyVenueMessageType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationProvider @Inject constructor(
    private val context: Context
) {

    init {
        createAreaRiskChangedNotificationChannel()
        createIsolationStateNotificationChannel()
        createTestResultsNotificationChannel()
        createAppAvailabilityNotificationChannel()
        createRecommendedUpdatesNotificationChannel()
        createAppConfigurationNotificationChannel()
        createBackgroundWorkNotificationChannel()
        createShareKeysReminderNotificationChannel()
        createLocalMessageNotificationChannel()
        createIsolationHubReminderNotificationChannel()
    }

    companion object {
        const val RISK_CHANGED_CHANNEL_ID = "AREA_RISK_CHANGED"
        const val ISOLATION_STATE_CHANNEL_ID = "ISOLATION_STATE"
        const val TEST_RESULTS_CHANNEL_ID = "TEST_RESULTS"
        const val APP_AVAILABILITY_CHANNEL_ID = "APP_AVAILABILITY"
        const val RECOMMENDED_APP_UPDATE_CHANNEL_ID = "RECOMMENDED_APP_UPDATE"
        const val APP_CONFIGURATION_CHANNEL_ID = "APP_CONFIGURATION"
        const val BACKGROUND_WORK_CHANNEL_ID = "BACKGROUND_WORK"
        const val SHARE_KEYS_REMINDER_CHANNEL_ID = "SHARE_KEYS_REMINDER"
        const val LOCAL_MESSAGE_CHANNEL_ID = "LOCAL_MESSAGE"
        const val ISOLATION_HUB_REMINDER_CHANNEL_ID = "ISOLATION_HUB_REMINDER"
        const val AREA_RISK_CHANGED_NOTIFICATION_ID = 0
        const val RISKY_VENUE_VISIT_NOTIFICATION_ID = 1
        const val STATE_EXPIRATION_NOTIFICATION_ID = 2
        const val STATE_EXPOSURE_NOTIFICATION_ID = 3
        const val TEST_RESULTS_NOTIFICATION_ID = 4
        const val APP_AVAILABLE_NOTIFICATION_ID = 5
        const val APP_NOT_AVAILABLE_NOTIFICATION_ID = 6
        const val EXPOSURE_REMINDER_NOTIFICATION_ID = 7
        const val RECOMMENDED_APP_UPDATE_NOTIFICATION_ID = 9
        const val SHARE_KEY_REMINDER_NOTIFICATION_ID = 10
        const val LOCAL_MESSAGE_NOTIFICATION_ID = 11
        const val ISOLATION_HUB_REMINDER_NOTIFICATION_ID = 12

        const val REQUEST_CODE_APP_IS_NOT_AVAILABLE = 1
        const val REQUEST_CODE_APP_IS_AVAILABLE = 2
        const val REQUEST_CODE_NOTIFICATION_REMINDER_CONTENT_INTENT = 3
        const val REQUEST_CODE_NOTIFICATION_REMINDER_ACTION_INTENT = 4
        const val REQUEST_CODE_SHOW_TEST_RESULTS = 5
        const val REQUEST_CODE_SHOW_EXPOSURE_NOTIFICATION = 6
        const val REQUEST_CODE_SHOW_STATE_EXPIRATION_NOTIFICATION = 7
        const val REQUEST_CODE_SHOW_RISKY_VENUE_VISIT_NOTIFICATION = 8
        const val REQUEST_CODE_SHOW_AREA_RISK_CHANGED_NOTIFICATION = 9
        const val REQUEST_CODE_UPDATING_DATABASE_NOTIFICATION = 10
        const val REQUEST_CODE_RECOMMENDED_APP_UPDATE = 12
        const val REQUEST_CODE_SHARE_KEYS_REMINDER = 13
        const val REQUEST_CODE_LOCAL_MESSAGE_NOTIFICATION = 14
        const val REQUEST_CODE_ISOLATION_HUB_REMINDER_NOTIFICATION = 15

        const val CONTACT_TRACING_HUB_ACTION = "CONTACT_TRACING_HUB_ACTION"
        const val TAPPED_ON_LOCAL_MESSAGE_NOTIFICATION = "TAPPED_ON_LOCAL_MESSAGE_NOTIFICATION"
        const val RISKY_VENUE_NOTIFICATION_TAPPED_WITH_TYPE = "RISKY_VENUE_NOTIFICATION_TAPPED_WITH_TYPE"
        const val TAPPED_ON_ISOLATION_HUB_REMINDER_NOTIFICATION = "TAPPED_ON_ISOLATION_HUB_REMINDER_NOTIFICATION"
    }

    private fun createAreaRiskChangedNotificationChannel() {
        createNotificationChannel(
            channelId = RISK_CHANGED_CHANNEL_ID,
            channelNameResId = R.string.notification_channel_area_risk_changed_name,
            importance = NotificationManagerCompat.IMPORTANCE_MAX,
            channelDescriptionResId = R.string.notification_channel_area_risk_changed_description
        )
    }

    private fun createIsolationStateNotificationChannel() {
        createNotificationChannel(
            channelId = ISOLATION_STATE_CHANNEL_ID,
            channelNameResId = R.string.notification_channel_isolation_state_name,
            importance = NotificationManagerCompat.IMPORTANCE_HIGH,
            channelDescriptionResId = R.string.notification_channel_isolation_state_description
        )
    }

    private fun createTestResultsNotificationChannel() {
        createNotificationChannel(
            channelId = TEST_RESULTS_CHANNEL_ID,
            channelNameResId = R.string.notification_channel_test_results_name,
            importance = NotificationManagerCompat.IMPORTANCE_HIGH,
            channelDescriptionResId = R.string.notification_channel_test_results_description
        )
    }

    private fun createAppAvailabilityNotificationChannel() {
        createNotificationChannel(
            channelId = APP_AVAILABILITY_CHANNEL_ID,
            channelNameResId = R.string.notification_channel_app_availability_name,
            importance = NotificationManagerCompat.IMPORTANCE_HIGH,
            channelDescriptionResId = R.string.notification_channel_app_availability_description
        )
    }

    private fun createRecommendedUpdatesNotificationChannel() {
        createNotificationChannel(
            channelId = RECOMMENDED_APP_UPDATE_CHANNEL_ID,
            channelNameResId = R.string.notification_channel_recommended_app_update_name,
            importance = NotificationManagerCompat.IMPORTANCE_HIGH,
            channelDescriptionResId = R.string.notification_channel_recommended_app_update_name_description
        )
    }

    private fun createAppConfigurationNotificationChannel() {
        createNotificationChannel(
            channelId = APP_CONFIGURATION_CHANNEL_ID,
            channelNameResId = R.string.notification_channel_app_configuration_name,
            importance = NotificationManagerCompat.IMPORTANCE_HIGH,
            channelDescriptionResId = R.string.notification_channel_app_configuration_description
        )
    }

    private fun createBackgroundWorkNotificationChannel() {
        createNotificationChannel(
            channelId = BACKGROUND_WORK_CHANNEL_ID,
            channelNameResId = R.string.notification_channel_background_work_name,
            importance = NotificationManagerCompat.IMPORTANCE_LOW,
            channelDescriptionResId = R.string.notification_channel_background_work_description
        )
    }

    private fun createShareKeysReminderNotificationChannel() {
        createNotificationChannel(
            channelId = SHARE_KEYS_REMINDER_CHANNEL_ID,
            channelNameResId = R.string.notification_channel_share_keys_reminder_name,
            importance = NotificationManagerCompat.IMPORTANCE_DEFAULT,
            channelDescriptionResId = R.string.notification_channel_share_keys_reminder_description
        )
    }

    private fun createLocalMessageNotificationChannel() {
        createNotificationChannel(
            channelId = LOCAL_MESSAGE_CHANNEL_ID,
            channelNameResId = R.string.notification_channel_local_message_name,
            importance = NotificationManagerCompat.IMPORTANCE_DEFAULT,
            channelDescriptionResId = R.string.notification_channel_local_message_description
        )
    }

    private fun createIsolationHubReminderNotificationChannel() {
        createNotificationChannel(
            channelId = ISOLATION_HUB_REMINDER_CHANNEL_ID,
            channelNameResId = R.string.notification_channel_isolation_hub_reminder_name,
            importance = NotificationManagerCompat.IMPORTANCE_DEFAULT,
            channelDescriptionResId = R.string.notification_channel_isolation_hub_reminder_description
        )
    }

    fun showAreaRiskChangedNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_SHOW_AREA_RISK_CHANGED_NOTIFICATION,
                intent,
                FLAG_UPDATE_CURRENT
            )

        val areaRiskChangedNotification = createNotification(
            RISK_CHANGED_CHANNEL_ID,
            R.string.notification_title_post_code_risk_changed,
            R.string.notification_text_post_code_risk_changed,
            pendingIntent
        )

        NotificationManagerCompat.from(context)
            .notify(
                AREA_RISK_CHANGED_NOTIFICATION_ID,
                areaRiskChangedNotification
            )
    }

    fun showRiskyVenueVisitNotification(messageType: RiskyVenueMessageType) {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(RISKY_VENUE_NOTIFICATION_TAPPED_WITH_TYPE, messageType)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_SHOW_RISKY_VENUE_VISIT_NOTIFICATION,
                intent,
                FLAG_UPDATE_CURRENT
            )

        val riskyVenueNotification = createNotification(
            RISK_CHANGED_CHANNEL_ID,
            title = R.string.app_name,
            message = R.string.notification_title_risky_venue,
            contentIntent = pendingIntent
        )

        NotificationManagerCompat.from(context)
            .notify(
                RISKY_VENUE_VISIT_NOTIFICATION_ID,
                riskyVenueNotification
            )
    }

    fun showStateExpirationNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_SHOW_STATE_EXPIRATION_NOTIFICATION,
                intent,
                FLAG_UPDATE_CURRENT
            )

        val expirationNotification = createNotification(
            ISOLATION_STATE_CHANNEL_ID,
            R.string.notification_title_state_expiration,
            R.string.notification_text_state_expiration,
            pendingIntent
        )

        NotificationManagerCompat.from(context)
            .notify(
                STATE_EXPIRATION_NOTIFICATION_ID,
                expirationNotification
            )
    }

    fun showExposureNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_SHOW_EXPOSURE_NOTIFICATION,
                intent,
                FLAG_UPDATE_CURRENT
            )

        val exposureNotification = createNotification(
            ISOLATION_STATE_CHANNEL_ID,
            title = context.getString(R.string.app_name),
            message = context.getString(R.string.notification_title_state_exposure),
            contentIntent = pendingIntent
        )

        NotificationManagerCompat.from(context)
            .notify(
                STATE_EXPOSURE_NOTIFICATION_ID,
                exposureNotification
            )
    }

    fun showExposureNotificationReminder() {
        val contentIntentActivity = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(CONTACT_TRACING_HUB_ACTION, ONLY_NAVIGATE)
        }
        val contentIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION_REMINDER_CONTENT_INTENT,
                contentIntentActivity,
                FLAG_UPDATE_CURRENT
            )

        val actionIntentActivity = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(CONTACT_TRACING_HUB_ACTION, NAVIGATE_AND_TURN_ON)
        }
        val actionIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION_REMINDER_ACTION_INTENT,
                actionIntentActivity,
                FLAG_UPDATE_CURRENT
            )

        val exposureReminderNotification = createNotification(
            APP_CONFIGURATION_CHANNEL_ID,
            title = R.string.notification_title_exposure_reminder,
            message = R.string.empty,
            contentIntent = contentIntent,
            actionText = R.string.notification_action_exposure_reminder,
            actionIntent = actionIntent
        )

        NotificationManagerCompat.from(context)
            .notify(
                EXPOSURE_REMINDER_NOTIFICATION_ID,
                exposureReminderNotification
            )
    }

    fun showTestResultsReceivedNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_SHOW_TEST_RESULTS,
                intent,
                FLAG_UPDATE_CURRENT
            )

        val testResultsReceivedNotification = createNotification(
            TEST_RESULTS_CHANNEL_ID,
            R.string.notification_title_test_results,
            R.string.notification_text_test_results,
            pendingIntent
        )

        NotificationManagerCompat.from(context)
            .notify(
                TEST_RESULTS_NOTIFICATION_ID,
                testResultsReceivedNotification
            )
    }

    fun showShareKeysReminderNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_SHARE_KEYS_REMINDER,
                intent,
                FLAG_UPDATE_CURRENT
            )

        val shareKeysReminderNotification = createNotification(
            SHARE_KEYS_REMINDER_CHANNEL_ID,
            R.string.notification_title_share_keys_reminder,
            R.string.notification_text_share_keys_reminder,
            pendingIntent
        )

        NotificationManagerCompat.from(context)
            .notify(
                SHARE_KEY_REMINDER_NOTIFICATION_ID,
                shareKeysReminderNotification
            )
    }

    fun cancelTestResult() {
        NotificationManagerCompat.from(context)
            .cancel(TEST_RESULTS_NOTIFICATION_ID)
    }

    fun cancelExposureNotification() {
        NotificationManagerCompat.from(context)
            .cancel(STATE_EXPOSURE_NOTIFICATION_ID)
    }

    fun cancelIsolationHubReminderNotification() {
        NotificationManagerCompat.from(context)
            .cancel(ISOLATION_HUB_REMINDER_NOTIFICATION_ID)
    }

    fun showAppIsAvailable() {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_APP_IS_AVAILABLE,
                intent,
                FLAG_UPDATE_CURRENT
            )

        val appAvailableNotification = createNotification(
            APP_AVAILABILITY_CHANNEL_ID,
            R.string.notification_title_app_available,
            R.string.notification_text_app_availability,
            pendingIntent
        )

        NotificationManagerCompat.from(context)
            .notify(
                APP_AVAILABLE_NOTIFICATION_ID,
                appAvailableNotification
            )
    }

    fun showAppIsNotAvailable() {
        val intent = Intent(context, AppAvailabilityActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_APP_IS_NOT_AVAILABLE,
                intent,
                FLAG_UPDATE_CURRENT
            )

        val appAvailableNotification = createNotification(
            APP_AVAILABILITY_CHANNEL_ID,
            R.string.notification_title_app_not_available,
            R.string.notification_text_app_availability,
            pendingIntent
        )

        NotificationManagerCompat.from(context)
            .notify(
                APP_NOT_AVAILABLE_NOTIFICATION_ID,
                appAvailableNotification
            )
    }

    fun showLocalMessageNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(TAPPED_ON_LOCAL_MESSAGE_NOTIFICATION, true)
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_LOCAL_MESSAGE_NOTIFICATION,
                intent,
                FLAG_UPDATE_CURRENT
            )

        val localMessageNotification = createNotification(LOCAL_MESSAGE_CHANNEL_ID, title, message, pendingIntent)

        NotificationManagerCompat.from(context)
            .notify(
                LOCAL_MESSAGE_NOTIFICATION_ID,
                localMessageNotification
            )
    }

    fun showRecommendedAppUpdateIsAvailable() {
        val intent = Intent(context, UpdateRecommendedActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(UpdateRecommendedActivity.STARTED_FROM_NOTIFICATION, true)
        }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_RECOMMENDED_APP_UPDATE,
                intent,
                FLAG_UPDATE_CURRENT
            )

        val recommendedAppUpdateNotification = createNotification(
            RECOMMENDED_APP_UPDATE_CHANNEL_ID,
            R.string.notification_title_recommended_app_update,
            R.string.notification_text_recommended_app_update,
            pendingIntent
        )

        NotificationManagerCompat.from(context)
            .notify(RECOMMENDED_APP_UPDATE_NOTIFICATION_ID, recommendedAppUpdateNotification)
    }

    fun showIsolationHubReminderNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(TAPPED_ON_ISOLATION_HUB_REMINDER_NOTIFICATION, true)
        }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_ISOLATION_HUB_REMINDER_NOTIFICATION,
                intent,
                FLAG_UPDATE_CURRENT
            )

        val isolationHubReminderNotification = createNotification(
            ISOLATION_HUB_REMINDER_CHANNEL_ID,
            R.string.notification_title_isolation_hub_reminder,
            R.string.notification_text_isolation_hub_reminder,
            pendingIntent
        )

        NotificationManagerCompat.from(context)
            .notify(ISOLATION_HUB_REMINDER_NOTIFICATION_ID, isolationHubReminderNotification)
    }

    fun getUpdatingDatabaseNotification(): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE_UPDATING_DATABASE_NOTIFICATION,
                intent,
                FLAG_UPDATE_CURRENT
            )

        return createNotification(
            BACKGROUND_WORK_CHANNEL_ID,
            R.string.app_name,
            R.string.notification_text_updating_database,
            pendingIntent,
            useCategoryAlarm = false
        )
    }

    fun isChannelEnabled(channelId: String): Boolean {
        val manager = NotificationManagerCompat.from(context)
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val channel = manager.getNotificationChannel(channelId)
            if (channel?.importance == NotificationManager.IMPORTANCE_NONE) {
                return false
            }
        }
        return manager.areNotificationsEnabled()
    }

    private fun createNotificationChannel(
        channelId: String,
        @StringRes channelNameResId: Int,
        importance: Int,
        @StringRes channelDescriptionResId: Int
    ) {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, context.getString(channelNameResId), importance
            ).apply {
                description = context.getString(channelDescriptionResId)
            }
            NotificationManagerCompat.from(context).createNotificationChannel(channel)
        }
    }

    private fun createNotification(
        notificationChannel: String,
        @StringRes title: Int,
        @StringRes message: Int,
        contentIntent: PendingIntent,
        @StringRes actionText: Int? = null,
        actionIntent: PendingIntent? = null,
        autoCancel: Boolean = true,
        useCategoryAlarm: Boolean = true
    ): Notification {
        return createNotification(
            notificationChannel,
            context.getString(title),
            context.getString(message),
            contentIntent,
            actionText,
            actionIntent,
            autoCancel,
            useCategoryAlarm
        )
    }

    private fun createNotification(
        notificationChannel: String,
        title: String,
        message: String,
        contentIntent: PendingIntent,
        @StringRes actionText: Int? = null,
        actionIntent: PendingIntent? = null,
        autoCancel: Boolean = true,
        useCategoryAlarm: Boolean = true
    ) =
        NotificationCompat.Builder(context, notificationChannel)
            .setSmallIcon(R.mipmap.ic_notification)
            .apply {
                if (useCategoryAlarm) {
                    setCategory(NotificationCompat.CATEGORY_ALARM) // Shows notification in DND mode
                }
            }
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .apply {
                actionText?.let {
                    addAction(
                        0,
                        context.getString(it),
                        actionIntent ?: contentIntent
                    )
                }
            }
            .setAutoCancel(autoCancel)
            .setContentIntent(contentIntent)
            .build()

    enum class ContactTracingHubAction {
        ONLY_NAVIGATE, NAVIGATE_AND_TURN_ON
    }
}
