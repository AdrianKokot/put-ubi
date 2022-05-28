package com.kokotadrian.boardgamegeekviewer

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
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
                id NUMERIC PRIMARY KEY,
                title TEXT, 
                release_year INTEGER,
                current_ranking INTEGER,
                game_thumbnail TEXT,
                type TEXT
            )
        """.trimIndent()

        val CREATE_CONFIG_TABLE = """
            CREATE TABLE config ( 
                username TEXT PRIMARY KEY,
                last_sync timestamp
            )
        """.trimIndent()

        val CREATE_RANK_TABLE = """
            CREATE TABLE game_ranks ( 
                id NUMERIC,
                rank INTEGER,
                date timestamp default current_date,
                PRIMARY KEY (id, date)
            )
        """.trimIndent()

        db.execSQL(CREATE_CONFIG_TABLE);
        db.execSQL(CREATE_GAME_TABLE);
        db.execSQL(CREATE_RANK_TABLE);
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS games")
        db.execSQL("DROP TABLE IF EXISTS config")
        db.execSQL("DROP TABLE IF EXISTS game_ranks")
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

    fun getGameList(): MutableList<GameInfo> {
        val db = this.writableDatabase

        val cursor = db.rawQuery("SELECT * FROM games WHERE type = 'game'", null)

        val list = mutableListOf<GameInfo>()

        with(cursor) {
            while (moveToNext()) {
                list.add(
                    GameInfo(
                        getLong(getColumnIndexOrThrow("id")),
                        getString(getColumnIndexOrThrow("title")),
                        getInt(getColumnIndexOrThrow("release_year")),
                        getString(getColumnIndexOrThrow("game_thumbnail")),
                        getInt(getColumnIndexOrThrow("current_ranking"))
                    )
                )
            }
        }

        db.close()

        return list
    }

    fun getGameRankList(id: Long): MutableList<GameRank> {
        val db = this.writableDatabase

        val cursor = db.rawQuery("SELECT * FROM game_ranks WHERE id = " + id, null)

        val list = mutableListOf<GameRank>()

        with(cursor) {
            while (moveToNext()) {
                list.add(
                    GameRank(
                        Instant.parse(cursor.getString(getColumnIndexOrThrow("date"))),
                        getInt(getColumnIndexOrThrow("rank"))
                    )
                )
            }
        }

        db.close()

        return list
    }

    fun getExpansionList(): MutableList<ExpansionInfo> {
        val db = this.writableDatabase

        val cursor = db.rawQuery("SELECT * FROM games WHERE type = 'expansion'", null)

        val list = mutableListOf<ExpansionInfo>()

        with(cursor) {
            while (moveToNext()) {
                list.add(
                    ExpansionInfo(
                        getLong(getColumnIndexOrThrow("id")),
                        getString(getColumnIndexOrThrow("title")),
                        getInt(getColumnIndexOrThrow("release_year")),
                        getString(getColumnIndexOrThrow("game_thumbnail"))
                    )
                )
            }
        }

        db.close()

        return list
    }

    fun assignUsername(username: String) {
        val values = ContentValues()

        values.put("username", username)
        val db = this.writableDatabase

        db.insert("config", null, values)
        db.close()
    }

    private fun setLastSyncToNow() {
        val values = ContentValues()
        values.put("last_sync", Instant.now().toString())

        val db = this.writableDatabase

        db.update("config", values, null, null)
        db.close()
    }

    fun eraseData() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM games")
        db.execSQL("DELETE FROM config")
        db.execSQL("DELETE FROM game_ranks")
        db.close()
    }

    private fun syncRank(list: MutableList<CollectionItemInfo>) {
        val sql = "INSERT OR REPLACE INTO game_ranks (id, rank) VALUES (?, ?)"
        val db = this.writableDatabase

        val statement = db.compileStatement(sql)

        db.beginTransaction()

        try {
            for (info in list) {
                if (info.type == "game") {
                    statement.clearBindings()
                    statement.bindLong(1, info.id)
                    statement.bindLong(2, info.boardGameRank.toLong())
                    statement.execute()
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun sync(list: MutableList<CollectionItemInfo>) {
        val sql = "INSERT OR REPLACE INTO games VALUES (?, ?, ?, ?, ?, ?)"
        val db = this.writableDatabase

        db.execSQL("DELETE FROM games")

        val statement = db.compileStatement(sql)

        db.beginTransaction()

        try {
            for (info in list) {
                statement.clearBindings()
                statement.bindLong(1, info.id)
                statement.bindString(2, info.name)
                statement.bindLong(3, info.yearPublished.toLong())
                statement.bindLong(4, info.boardGameRank.toLong())
                statement.bindString(5, info.thumbnail)
                statement.bindString(6, info.type)
                statement.execute()
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }

        this.syncRank(list)

        this.setLastSyncToNow()
    }

}
