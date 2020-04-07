package com.bitmark.autonomy.feature.notification

object NotificationPayloadType {

    const val NOTIFICATION_TYPE = "notification_type"

    const val HELP_ID = "help_id"
}

object NotificationId {
    const val SURVEY = 0x01

    const val NEW_HELP_REQUEST = 0x02

    const val ACCEPTED_HELP_REQUEST = 0x03
}

object NotificationType {

    const val NEW_HELP_REQUEST = "BROADCAST_NEW_HELP"

    const val ACCEPTED_HELP_REQUEST = "NOTIFY_HELP_ACCEPTED"

    const val HELP_REQUEST_EXPIRED = "NOTIFY_HELP_EXPIRED"
}