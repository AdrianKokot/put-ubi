package com.kokotadrian.boardgamegeekviewer

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.Timestamp
import java.time.Instant

class AppDBHandler(
    context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "games.db"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_GAME_TABLE = """
            CREATE TABLE games (
                id INTEGER PRIMARY KEY,
                title TEXT, 
                release_year INTEGER,
                bgg_id NUMERIC,
                current_ranking INTEGER,
                game_thumbnail TEXT
            )
        """.trimIndent()

        val CREATE_CONFIG_TABLE = """
            CREATE TABLE config ( 
                username TEXT PRIMARY KEY,
                last_sync timestamp
            )
        """.trimIndent()

        db.execSQL(CREATE_CONFIG_TABLE);
        db.execSQL(CREATE_GAME_TABLE);
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS games")
        db.execSQL("DROP TABLE IF EXISTS config")
        onCreate(db)
    }

    fun getConfig(): AppConfig? {
        val query = "SELECT * FROM config LIMIT 1";
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null);
        var config: AppConfig? = null

        if (cursor.moveToFirst()) {
            val lastSyncString = cursor.getString(1)

            config = AppConfig(
                cursor.getString(0),
                if (lastSyncString.isNullOrEmpty()) null else Instant.parse(lastSyncString)
            )
            cursor.close();
        }

        db.close()
        return config;
    }

    fun assignUsername(username: String) {
        val values = ContentValues()

        values.put("username", username)
        val db = this.writableDatabase

        db.insert("config", null, values)
        db.close()
    }

    fun setLastSyncToNow() {
        val values = ContentValues()
        values.put("last_sync", Instant.now().toString())

        val db = this.writableDatabase

        db.update("config", values, null, null)
        db.close()
    }
}
