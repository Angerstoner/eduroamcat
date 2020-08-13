package de.unigoe.eduroamcat.backend.models

import android.net.wifi.WifiEnterpriseConfig
import android.os.Build
import de.unigoe.eduroamcat.backend.util.WifiEnterpriseConfigurator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * TODO: change to instrumentation test, since junit will not work here (calls to android api methods needed)
 * JUnit Test for creation of [android.net.wifi.WifiEnterpriseConfig] from a parsed EapConfig
 */
internal class WifiEnterpriseConfiguratorTest {
//    @Test
//    fun setServerIdTest() {
//        setFinalStatic(Build.VERSION::class.java.getField("SDK_INT"), Build.VERSION_CODES.M)
//
//        val testEapConfig = WifiEnterpriseConfig()
//        val serverIdList = listOf("radius1.umk.pl", "radius2.umk.pl")
//
//        testConfigurator.setServerIds(testEapConfig, serverIdList)
//        assertEquals("DNS:radius1.umk.pl;DNS:radius2.umk.pl", testEapConfig.altSubjectMatch)
//    }
//
//    private val testConfigurator = WifiEnterpriseConfigurator()
//
//    @Test
//    fun getConfigFromFileTest() {
//        testConfigurator.getConfigFromFile("src/test/res/XmlParserTestConfigFull.eap-config")
//    }
//
//    // Use reflections to mock field values (e.g. Build version)
//    @Throws(Exception::class)
//    private fun setFinalStatic(field: Field, newValue: Any?) {
//        field.isAccessible = true
//        val modifiersField: Field = Field::class.java.getDeclaredField("modifiers")
//        modifiersField.isAccessible = true
//        modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
//        field.set(null, newValue)
//    }
}