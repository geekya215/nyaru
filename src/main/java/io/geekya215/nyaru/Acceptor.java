package io.geekya215.nyaru;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class Acceptor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Acceptor.class.getName());

    private final ServerSocketChannel serverSocket;
    private final Selector selector;

    public Acceptor(ServerSocketChannel serverChannel, Selector selector) {
        this.serverSocket = serverChannel;
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            SocketChannel socket = serverSocket.accept();
            if (socket != null) {
//                LOGGER.log(Level.INFO, "Connection established");
                new Handler(selector, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
