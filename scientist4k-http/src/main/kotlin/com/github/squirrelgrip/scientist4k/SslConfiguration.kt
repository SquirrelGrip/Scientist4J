package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.extensions.file.toInputStream
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class SslConfiguration(
        val keyStorePath: String,
        val keyStorePassword: String,
        val keyStoreType: String = KeyStore.getDefaultType(),
        val trustStorePath: String,
        val trustStorePassword: String,
        val trustStoreType: String = KeyStore.getDefaultType(),
        val algorithm: String = "TLSv1.2"
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
                init(keyStore(), keyStorePassword.toCharArray())
            }

    fun trustStore(): KeyStore =
            File(trustStorePath).toKeyStore(trustStorePassword, trustStoreType)

    fun keyStore(): KeyStore =
            File(keyStorePath).toKeyStore(keyStorePassword, keyStoreType)
}

fun File.toKeyStore(password: String, keyStoreType: String = KeyStore.getDefaultType()): KeyStore =
        KeyStore.getInstance(keyStoreType).also {
            it.load(this.toInputStream(), password.toCharArray())
        }
