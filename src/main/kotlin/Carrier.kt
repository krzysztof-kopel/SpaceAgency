package org.example

import dev.kourier.amqp.channel.AMQPChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

suspend fun listen(channel: AMQPChannel, queueName: String) {
    val consumer = channel.basicConsume(queueName, noAck=false)

    for (task in consumer) {
        val taskName = task.message.body.decodeToString()
        println("Executing task: $taskName")
        channel.basicAck(task.message, multiple=false)
    }
}

fun main(args: Array<String>) = runBlocking(Dispatchers.IO) {
    if (args.size != 2 || !args.all{it in arrayOf("people", "cargo", "satellite")}) {
        println("Wrong input")
        return@runBlocking
    }

    val channel = configureSystem(this)
    launch{ listenFromAdmin(channel, "carrier") }

    args.forEach{
        channel.queueDeclare(it, exclusive=false)
        channel.queueBind(it, "ex", "*.$it")
    }

    channel.basicQos(count=1u, global=false)

    args.forEach{this.launch{listen(channel, it)}}
}
