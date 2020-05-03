package com.github.squirrelgrip.scientist4k.http.core.configuration

import com.github.squirrelgrip.extension.file.toInputStream
import com.github.squirrelgrip.scientist4k.http.core.exception.InvalidConfigurationException
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

open class SslConfiguration(
        val keyStorePath: String = "",
        val keyStorePassword: String = "",
        val keyStoreType: String = KeyStore.getDefaultType(),
        val trustStorePath: String = "",
        val trustStorePassword: String = "",
        val trustStoreType: String = KeyStore.getDefaultType(),
        val algorithm: String = "TLS"
) {

    fun sslContext(): SSLContext = SSLContext.getInstance(algorithm).apply {
        init(keyManagerFactory().keyManagers, trustManagerFactory().trustManagers, SecureRandom())
    }

    fun trustManagerFactory(): TrustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
                init(trustStore())
            }

    fun keyManagerFactory(): KeyManagerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
                init(keyStore(), keyStorePassword.password())
            }

    fun trustStore(): KeyStore {
        if (trustStoreType == "Windows-ROOT") {
            return KeyStore.getInstance(trustStoreType).apply {
                load(null, null)
            }
        }
        return File(trustStorePath).toKeyStore(trustStorePassword, trustStoreType)
    }

    fun keyStore(): KeyStore {
        if (keyStoreType == "Windows-MY") {
            return KeyStore.getInstance(keyStoreType).apply {
                load(null, null)
            }
        }
        return File(keyStorePath).toKeyStore(keyStorePassword, keyStoreType)
    }
}

fun File.toKeyStore(password: String, keyStoreType: String = KeyStore.getDefaultType()): KeyStore =
        KeyStore.getInstance(keyStoreType).also {
            it.load(this.toInputStream(), password.password())
        }

fun String.password(): CharArray {
    try {
        val prefix = this.split(":").first()
        val value = this.substring(prefix.length + 1)
        return when (prefix) {
            "pass" -> value
            "env" -> System.getenv(value)
            "file" -> File(value).readText()
            else -> throw InvalidConfigurationException("Unknown password prefix; should be one of pass:, env: or file:")
        }.toCharArray()
    } catch (e: StringIndexOutOfBoundsException) {
        return this.toCharArray()
    }
}