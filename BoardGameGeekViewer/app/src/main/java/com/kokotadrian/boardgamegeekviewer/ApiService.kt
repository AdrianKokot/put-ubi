package com.kokotadrian.boardgamegeekviewer

import android.os.AsyncTask
import android.util.Log
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
}


interface XmlApiParser<T> {
    public fun parse(xmlDoc: Document): T;
}

class UserProfile(val id: Long, val name: String) {
}

class UserXmlApiParser : XmlApiParser<UserProfile?> {
    override fun parse(xmlDoc: Document): UserProfile? {

        val element = xmlDoc.getElementsByTagName("user").item(0) as Element
        val idAttrib = element.getAttribute("id");

        if(idAttrib.isNullOrEmpty()) {
            return null
        }

        return UserProfile(idAttrib.toLong(), element.getAttribute("name"))
    }
}

@Suppress("DEPRECATION")
class XmlApiDownloader<T>(
    private val parser: XmlApiParser<T>,
    private val stringUrl: String
) : AsyncTask<String, Int, T>() {
    private var listener: (T) -> Unit = {}

    fun addOnFinishListener(callback: (T) -> Unit) {
        this.listener = callback
    }

    override fun onPostExecute(result: T) {
        super.onPostExecute(result)
        this.listener(result)
    }

    override fun doInBackground(vararg p0: String?): T? {
        try {
            val url = URL(this.stringUrl)
            val inputStream = url.openConnection().getInputStream()

            val xmlDoc =
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
            xmlDoc.documentElement.normalize()

            inputStream.close()

            return this.parser.parse(xmlDoc);
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null;
    }
}
