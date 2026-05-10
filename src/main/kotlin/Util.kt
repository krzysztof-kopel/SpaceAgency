package org.example

import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.amqpConfig
import dev.kourier.amqp.connection.createAMQPConnection
import kotlinx.coroutines.CoroutineScope

suspend fun listenFromAdmin(channel: AMQPChannel, userType: String, userName:String) {
    val uniqueQueueName = "admin_to_$userName"
    channel.queueDeclare(uniqueQueueName, exclusive=true, autoDelete=true)

    channel.queueBind(uniqueQueueName, "ex", "admin.$userType")
    channel.queueBind(uniqueQueueName, "ex", "admin.all")

    val consumer = channel.basicConsume(uniqueQueueName)

    for (message in consumer) {
        println("Message from admin: ${message.message.body.decodeToString()}")
    }
}

suspend fun configureSystem(scope: CoroutineScope): AMQPChannel {
    val config = amqpConfig {
        server {
            host = "127.0.0.1"
        }
    }

    val connection = createAMQPConnection(scope, config)
    val channel = connection.openChannel()

    channel.exchangeDeclare("ex", "topic")

    return channel
}