package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>): Unit = runBlocking(Dispatchers.IO) {
    if (args.size != 1) {
        println("Wrong input")
        return@runBlocking
    }
    val agencyName = args[0]
    println("Agency name: $agencyName")
    var actionNumber = 1

    val channel = configureSystem(this)
    launch{ listenFromAdmin(channel, "agency") }

    while (true) {
        println("Select service:\n1. Send people\n2. Send cargo\n3. Send a satellite\n4. Exit")
        val service = readln()

        val serviceType = when (service) {
            "1" -> "people"
            "2" -> "cargo"
            "3" -> "satellite"
            "4" -> "exit"
            else -> null
        } ?: run {println("Provide a valid service name"); continue}

        if (serviceType == "exit") {
            break
        }

        val routingKey = "$agencyName.$serviceType"
        val message = "type $serviceType, from $agencyName (number ${actionNumber++})"
        channel.basicPublish(message.toByteArray(), "ex", routingKey)
        println("Sent $message")
    }

    channel.close()
}
