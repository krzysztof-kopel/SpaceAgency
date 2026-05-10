package org.example

import dev.kourier.amqp.channel.AMQPChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

suspend fun sender(channel: AMQPChannel) {
    while (true) {
        println("Send a message to agencies (A:), carriers (C:) or everyone (E:)")
        val message = readln()

        val routingKey = when (message.substring(0, 2)) {
            "A:" -> "admin.agency"
            "C:" -> "admin.carrier"
            "E:" -> "admin.all"
            else -> null
        } ?: run{ println("Wrong type of message"); continue}

        channel.basicPublish(message.substring(2).toByteArray(), "ex", routingKey)
    }
}

fun main(): Unit = runBlocking(Dispatchers.IO) {
    val channel = configureSystem(this)

    channel.queueDeclare("admin_listener", exclusive=true, autoDelete=true)
    channel.queueBind("admin_listener", "ex", "#")
    val consumer = channel.basicConsume("admin_listener")

    launch{ sender(channel) }

    for (message in consumer) {
        println("Received: ${message.message.body.decodeToString()}")
    }
}