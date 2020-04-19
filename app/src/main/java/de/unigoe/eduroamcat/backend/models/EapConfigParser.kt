package de.unigoe.eduroamcat.backend.models

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.File

class EapConfigParser {
    companion object {
        fun fromXml(eapConfigFile: String) {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(File(eapConfigFile).inputStream().reader())
            parser.nextTag()

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.TEXT)
                    Log.i("EAPConfig", "" + parser.text)
            }
        }
    }
}