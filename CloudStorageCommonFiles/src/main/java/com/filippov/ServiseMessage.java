package com.filippov;
import io.netty.channel.Channel;

import java.io.Serializable;

public class ServiseMessage implements Serializable {
    String message;

    private ServiseMessage(String message) {
        this.message = message;
    }

    public static void sendMessage(Channel channel, String message) {
        channel.writeAndFlush(new ServiseMessage(message));
    }

    public String getMessage() {
        return message;
    }
}
