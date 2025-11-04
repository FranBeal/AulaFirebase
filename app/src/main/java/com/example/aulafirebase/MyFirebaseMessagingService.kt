package com.example.aulafirebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.remoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService(){
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.notification?.let{
            println("Mensagem de Notificação recebida: " +
                    "${it.body}")
        }

        message.data.isNotEmpty().let{
            println("Dados: ${message.data}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("Novo token gerado/atualizado: " +
                "$token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String){

    }
}