package com.kokotadrian.boardgamegeekviewer

import java.sql.Timestamp
import java.time.Instant

class AppConfig(username: String, lastSync: Instant?) {
    var lastSync: Instant? = lastSync
    var username: String = username
}