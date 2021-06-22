package com.childrenofcorn.eldorado

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.lang.Exception

class ProductParser(var url: String?) {

    fun fixURL() {
        if (url != null) {
            if (!url.toString().contains("http://")) {
                if (!url.toString().contains("https://")) {
                    url = "https://$url"
                }
            } else {
                url = "https://" + url.toString().substring(7)
            }
        }
    }

    fun getDocument() : Document {

        try {
            fixURL()
            val page = Jsoup.connect(url)
                .userAgent(
                    "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"
                ).get()
            return page
        } catch (e: Exception) {
            Log.d("Exception!", e.toString())
        }

        throw Exception()
    }

    fun getString(doc: Document, string: String) : Elements {
        var res = doc.select(string)
        return res
    }

    fun getProductName(doc: Document) : String {
        var elements = doc.select("h1.catalogItemDetailHd")
        return elements[0].text()
    }

    fun getProductPrice(doc: Document) : String {
        var elements = doc.select("div.product-box-price__active")
        Log.d("ProductPrice", elements[0].text())
        return elements[0].text()
    }

    fun getProductImageLink(doc: Document) : String {
        var element = doc.select("img.slider-item-image.slider-item-image--lazy").first()
        var url = element.absUrl("data-src")
        Log.d("Product Image Link", url.toString())
        return url.toString()
    }

    fun getProductURL() : String {
        if (url == null) {
            return "";
        } else {
            return url.toString();
        }
    }
}