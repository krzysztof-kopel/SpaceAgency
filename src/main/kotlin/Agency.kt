package org.example

import dev.kourier.amqp.connection.amqpConfig
import dev.kourier.amqp.connection.createAMQPConnection
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>): Unit = runBlocking {
    if (args.size != 1) {
        println("Wrong input")
        return@runBlocking
    }
    val agencyName = args[0]
    println("Agency name: $agencyName")
    var actionNumber = 1

    val config = amqpConfig {
        server {
            host = "127.0.0.1"
        }
    }

    val connection = createAMQPConnection(this, config)
    val channel = connection.openChannel()

    channel.queueDeclare("people", exclusive=false)
    channel.queueDeclare("cargo", exclusive=false)
    channel.queueDeclare("satellite", exclusive=false)

    while (true) {
        println("Select service:\n1. Send people\n2. Send cargo\n3. Send a satellite\n4. Exit")
        val service = readln()

        val routingKey = when (service) {
            "1" -> "people"
            "2" -> "cargo"
            "3" -> "satellite"
            "4" -> "exit"
            else -> null
        } ?: run {println("Provide a valid service name"); continue}

        if (routingKey == "exit") {
            break
        }

        val message = "$agencyName-$routingKey-${actionNumber++}"
        channel.basicPublish(message.toByteArray(), "", routingKey)
        println("Sent $message")
    }

    channel.close()
    connection.close()
}
