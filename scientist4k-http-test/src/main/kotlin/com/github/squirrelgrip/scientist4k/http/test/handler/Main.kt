package com.github.squirrelgrip.scientist4k.http.test.handler

import com.github.squirrelgrip.cheti.Cheti
import com.github.squirrelgrip.scientist4k.http.core.configuration.SslConfiguration
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer

class Main {
    val controlServer: SecuredServer
    val referenceServer: SecuredServer
    val candidateServer: SecuredServer

    companion object {
        val sslConfiguration = SslConfiguration(
        "../certs/keystore.jks",
        "pass:password",
        "JKS",
        "../certs/keystore.jks",
        "pass:password",
        "JKS",
        "TLSv1.2"
        )
    }

    init {
        val chetiConfiguration = Thread.currentThread().contextClassLoader.getResourceAsStream("cheti.json")
        val cheti = Cheti(chetiConfiguration)
        cheti.execute()

        controlServer = SecuredServer(ControlHandler.serverConfiguration, ControlHandler())
        referenceServer = SecuredServer(ReferenceHandler.serverConfiguration, ReferenceHandler())
        candidateServer = SecuredServer(CandidateHandler.serverConfiguration, CandidateHandler())
    }

    fun startAll() {
        controlServer.start()
        referenceServer.start()
        candidateServer.start()
    }

    fun join() {
        controlServer.join()
    }
}

fun main() {
    val main = Main()
    main.startAll();
    main.join()
}