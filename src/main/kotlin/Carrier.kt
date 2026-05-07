package org.example

import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.amqpConfig
import dev.kourier.amqp.connection.createAMQPConnection
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

suspend fun listen(channel: AMQPChannel, queueName: String) {
    val consumer = channel.basicConsume(queueName, noAck=false)

    for (task in consumer) {
        val taskName = task.message.body.decodeToString()
        println("Executing task $taskName")
        channel.basicAck(task.message, multiple=false)
    }
}

fun main(args: Array<String>) = runBlocking {
    if (args.size != 2 || !args.all{it in arrayOf("people", "cargo", "satellite")}) {
        println("Wrong input")
        return@runBlocking
    }

    val config = amqpConfig {
        server {
            host = "127.0.0.2"
        }
    }
    val connection = createAMQPConnection(this, config)
    val channel = connection.openChannel()

    args.forEach{channel.queueDeclare(it, exclusive=false)}

    channel.basicQos(count=1u, global=false)

    args.forEach{this.launch{listen(channel, it)}}
}
