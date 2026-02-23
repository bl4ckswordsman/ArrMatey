package com.dnfapps.arrmatey.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

private val MIGRATION_1_2 = object: Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE instances ADD COLUMN headers TEXT NOT NULL DEFAULT '[]'"
        )
    }
}

private val MIGRATION_2_3 = object: Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            ALTER TABLE instances 
            ADD COLUMN localNetworkEnabled INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        connection.execSQL(
            """
            ALTER TABLE instances 
            ADD COLUMN localNetworkEndpoint TEXT DEFAULT NULL
            """.trimIndent()
        )

        connection.execSQL(
            """
            ALTER TABLE instances 
            ADD COLUMN localNetworkSsid TEXT DEFAULT NULL
            """.trimIndent()
        )
    }
}


val migrations = listOf(MIGRATION_1_2, MIGRATION_2_3)