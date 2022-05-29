package com.kokotadrian.boardgamegeekviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.xml.parsers.DocumentBuilderFactory


class ApiQueryBuilder {
    private val baseUrl = "https://www.boardgamegeek.com/xmlapi2/"

    fun userProfile(username: String): String {
        return baseUrl + "user?name=" + username;
    }

    fun userGames(username: String): String {
        return baseUrl + "collection?username=" + username + "&stats=1&subtype=boardgame&excludesubtype=boardgameexpansion"
    }

    fun userExpansions(username: String): String {
        return baseUrl + "collection?username=" + username + "&stats=1&subtype=boardgameexpansion"
    }
}


interface XmlApiParser<T> {
    fun parse(xmlDoc: Document): T;
}

class UserProfile(val id: Long, val name: String) {
}

class UserXmlApiParser : XmlApiParser<UserProfile?> {
    override fun parse(xmlDoc: Document): UserProfile? {

        if (xmlDoc.getElementsByTagName("errors").length == 0 && xmlDoc.getElementsByTagName("message").length > 0) {
            return null
        }

        val element = xmlDoc.getElementsByTagName("user").item(0) as Element
        val idAttrib = element.getAttribute("id");

        if (idAttrib.isNullOrEmpty()) {
            return UserProfile(-1, "missing")
        }

        return UserProfile(idAttrib.toLong(), element.getAttribute("name"))
    }
}

open class GameInfo(
    val id: Long,
    val name: String,
    val yearPublished: Int,
    val thumbnail: String,
    val boardGameRank: Int = 0
) {
}

class GameRank(val date: LocalDate, val rank: Int = -1) {
    fun getFormattedDate(): String {
        return DateTimeFormatter.ofPattern("d.MM.uuuu").format(date)
    }
}

class ExpansionInfo(
    val id: Long,
    val name: String,
    val yearPublished: Int,
    val thumbnail: String
) {}


class CollectionItemInfo(
    val id: Long,
    val name: String,
    val yearPublished: Int,
    val thumbnail: String,
    val boardGameRank: Int,
    val type: String
) {
    companion object {
        fun from(expansion: ExpansionInfo): CollectionItemInfo {
            return CollectionItemInfo(
                expansion.id,
                expansion.name,
                expansion.yearPublished,
                expansion.thumbnail,
                0,
                "expansion"
            )
        }

        fun from(game: GameInfo): CollectionItemInfo {
            return CollectionItemInfo(
                game.id,
                game.name,
                game.yearPublished,
                game.thumbnail,
                game.boardGameRank,
                "game"
            )
        }
    }
}

class UserCollectionXmlParser : XmlApiParser<MutableList<CollectionItemInfo>?> {
    override fun parse(xmlDoc: Document): MutableList<CollectionItemInfo>? {
        val collection = mutableListOf<CollectionItemInfo>()

        if (xmlDoc.getElementsByTagName("message").length > 0) {
            return null
        }

        val items = xmlDoc.getElementsByTagName("item")

        for (i in 0 until items.length) {
            if (items.item(i).nodeType == Node.ELEMENT_NODE) {
                val element = items.item(i) as Element

                val subtype = element.getAttribute("subtype")
                if (!(subtype.equals("boardgame") || subtype.equals("boardgameexpansion"))) {
                    continue
                }

                val type = if (subtype.equals("boardgame")) "game" else "expansion"

                val ranks = element.getElementsByTagName("rank")

                var rank = 0

                for (j in 0 until ranks.length) {
                    val rankElement = ranks.item(j) as Element
                    if (rankElement.getAttribute("type").equals("subtype") &&
                        rankElement.getAttribute("name").equals("boardgame")
                    ) {
                        rank = rankElement.getAttribute("value").toIntOrNull() ?: 0
                    }
                }

                val info = CollectionItemInfo(
                    element.getAttribute("objectid").toLong(),
                    element.getElementsByTagName("name").item(0).textContent.toString(),
                    element.getElementsByTagName("yearpublished").item(0)?.textContent?.toInt()
                        ?: -1,
                    element.getElementsByTagName("thumbnail").item(0)?.textContent?.toString()
                        ?: "",
                    rank,
                    type
                )

                collection.add(info)
            }
        }
        return collection.distinctBy { it.id }.toMutableList()
    }
}

@Suppress("DEPRECATION")
class XmlApiDownloader<T>(
    private val parser: XmlApiParser<T>,
    private val stringUrl: String
) : AsyncTask<String, Int, String>() {
    private var listener: (T?) -> Unit = {}
    private var result: T? = null

    fun addOnFinishListener(callback: (T?) -> Unit) {
        this.listener = callback
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        this.listener(this.result)
    }

    override fun doInBackground(vararg p0: String?): String {
        try {
            val url = URL(this.stringUrl)
            val inputStream = url.openConnection().getInputStream()

            val xmlDoc =
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
            xmlDoc.documentElement.normalize()

            inputStream.close()

            this.result = this.parser.parse(xmlDoc)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return "";
    }
}

object ImageCacheSingleton {
    private var collection = mutableMapOf<String, Bitmap>()

    fun getCachedValue(url: String): Bitmap? {
        return collection.getOrDefault(url, null)
    }

    fun setCache(url: String, value: Bitmap) {
        collection[url] = value
    }
}


@Suppress("DEPRECATION")
class DownloadImageTask(var imageView: ImageView) :
    AsyncTask<String, Void, Bitmap?>() {

    override fun doInBackground(vararg p0: String): Bitmap? {
        val url = p0[0]
        var bitmap: Bitmap? = null
        try {
            val stream = URL(url).openStream()
            bitmap = BitmapFactory.decodeStream(stream)
            ImageCacheSingleton.setCache(url, bitmap)
        } catch (e: java.lang.Exception) {
            Log.e("Error", e.message!!)
            e.printStackTrace()
        }
        return bitmap
    }

    override fun onPostExecute(result: Bitmap?) {
        imageView.setImageBitmap(result)
    }
}

object ListType {
    private var listType: String = ""

    fun setToGameList() {
        listType = "game"
    }

    fun setToExpansionList() {
        listType = "expansion"
    }

    fun isGameList(): Boolean {
        return listType == "game"
    }
}

object HistoryDetailsView {
    var viewedGame: CollectionItemInfo? = null
}