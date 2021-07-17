package com.canibal.appdoptafirebase.notifications;

public class Token {
    /*An FCN Token, or much commonly known as a registrationToken.
     * An ID issued by the GCM connection servers to the client app that allows it to receive messages*/
    /*Un token FCN, o comúnmente conocido como un registro token.
    Es una identificación emitida por los servidores de conexión GCM a la aplicación del cliente que le permite recibir mensajes*/

    String token;

    public Token(String token) {
        this.token = token;
    }

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
