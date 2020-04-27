package com.github.squirrelgrip.scientist4k.handler

import com.github.squirrelgrip.cheti.Cheti
import com.github.squirrelgrip.scientist4k.server.SecuredServer

class Main {
    val controlServer: SecuredServer
    val referenceServer: SecuredServer
    val candidateServer: SecuredServer

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