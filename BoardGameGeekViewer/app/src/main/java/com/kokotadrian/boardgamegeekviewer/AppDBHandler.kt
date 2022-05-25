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

    fun getGameList(): MutableList<GameInfo> {
        val db = this.writableDatabase

        val cursor = db.rawQuery("SELECT * FROM games", null)

        val list = mutableListOf<GameInfo>()

        with(cursor) {
            while (moveToNext()) {
                Log.i("Move", "moveToNext")
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

    private fun deleteGames() {
        val db = this.writableDatabase

        db.delete("games", "1 = 1", null)
        db.close()
    }

    fun syncGameCollection(gameInfoList: MutableList<GameInfo>) {
//        this.deleteGames()


        val sql = "INSERT INTO games VALUES (?, ?, ?, ?, ?)"
        val db = this.writableDatabase

        db.execSQL("DELETE FROM games")

        val statement = db.compileStatement(sql)

        db.beginTransaction()
        db.compileStatement("DELETE FROM games WHERE 1 = 1").execute()
        try {
            for (gameInfo in gameInfoList) {
                statement.clearBindings()
                statement.bindLong(1, gameInfo.id)
                statement.bindString(2, gameInfo.name)
                statement.bindLong(3, gameInfo.yearPublished.toLong())
                statement.bindLong(4, gameInfo.boardGameRank.toLong())
                statement.bindString(5, gameInfo.thumbnail)
//                val values = ContentValues().apply {
//                    put("id", gameInfo.id)
//                    put("title", gameInfo.name)
//                    put("release_year", gameInfo.yearPublished)
//                    put("current_ranking", gameInfo.boardGameRank)
//                    put("game_thumbnail", gameInfo.thumbnail)
//                }

                statement.execute()
//                val id =
//                    db.insertWithOnConflict("games", null, values, SQLiteDatabase.CONFLICT_REPLACE)
                Log.i("Inserted", "Executed")
            }
            db.setTransactionSuccessful()
        } finally {

            db.endTransaction()
        }
//        db.close()

    }

}
