package com.bitmark.autonomy.feature.notification

object NotificationConstants {
    val NOTIFICATION_HOUR_RANGE = 9..21

    val PUSH_COUNT_PER_DAY = (1..5).random()
}

object NotificationPayloadType {

    const val NOTIFICATION_TYPE = "notification_type"

    const val HELP_ID = "help_id"

    const val POI_ID = "poi_id"

    const val SYMPTOMS = "symptoms"
}

object NotificationId {

    const val SURVEY = 0x01

    const val NEW_HELP_REQUEST = 0x02

    const val ACCEPTED_HELP_REQUEST = 0x03

    const val RISK_LEVEL_CHANGED = 0x04

    const val CLEAN_AND_DISINFECT = 0x05

    const val ACCOUNT_SYMPTOM_FOLLOW_UP = 0x06

}

object NotificationType {

    const val NEW_HELP_REQUEST = "BROADCAST_NEW_HELP"

    const val ACCEPTED_HELP_REQUEST = "NOTIFY_HELP_ACCEPTED"

    const val HELP_REQUEST_EXPIRED = "NOTIFY_HELP_EXPIRED"

    const val RISK_LEVEL_CHANGED = "RISK_LEVEL_CHANGED"

    const val ACCOUNT_SYMPTOM_FOLLOW_UP = "ACCOUNT_SYMPTOM_FOLLOW_UP"
}

// need to be the corresponding string resource name
object ChannelId {

    const val IMPORTANT_ALERT = "important_alert"

    const val DEFAULT = "default_channel"
}