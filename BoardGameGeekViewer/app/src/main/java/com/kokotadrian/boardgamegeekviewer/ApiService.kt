package com.kokotadrian.boardgamegeekviewer

import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory


class ApiService {


}

class ApiQueryBuilder {
    private val baseUrl = "https://www.boardgamegeek.com/xmlapi2/"

    fun userProfile(username: String): String {
        return baseUrl + "user?name=" + username;
    }

    fun userGames(username: String): String {
        return baseUrl + "collection?username=" + username + "&stats=1&subtype=boardgame"
    }
}


interface XmlApiParser<T> {
    public fun parse(xmlDoc: Document): T;
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

class GameInfo(
    val id: Long,
    val name: String,
    val yearPublished: Int,
    val thumbnail: String,
    val boardGameRank: Int = 0
) {

}

class GamesXmlApiParser : XmlApiParser<MutableList<GameInfo>?> {
    override fun parse(xmlDoc: Document): MutableList<GameInfo>? {
        val collection = arrayListOf<GameInfo>()

        if (xmlDoc.getElementsByTagName("message").length > 0) {
            return null
        }

        val items = xmlDoc.getElementsByTagName("item")

        for (i in 0 until items.length) {
            if (items.item(i).nodeType == Node.ELEMENT_NODE) {
                val element = items.item(i) as Element

                if (!element.getAttribute("subtype").equals("boardgame")) {
                    continue
                }

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

                val gameInfo = GameInfo(
                    element.getAttribute("objectid").toLong(),
                    element.getElementsByTagName("name").item(0).textContent.toString(),
                    element.getElementsByTagName("yearpublished").item(0)?.textContent?.toInt() ?: -1,
                    element.getElementsByTagName("thumbnail").item(0)?.textContent?.toString() ?: "",
                    rank
                )

                collection.add(gameInfo)
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
