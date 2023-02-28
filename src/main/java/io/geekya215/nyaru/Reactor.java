package io.geekya215.nyaru;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Reactor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Reactor.class.getName());

    private final ServerSocketChannel serverSocket;
    private final Selector selector;

    public Reactor() throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.bind(new InetSocketAddress(9527));
        serverSocket.register(selector, SelectionKey.OP_ACCEPT)
            .attach(new Acceptor(serverSocket, selector));

        LOGGER.log(Level.INFO, "Server start at port " + 9527);
    }

    @Override
    public void run() {
        try {
            startLoop();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error occurred. Stopping server", e);
        } finally {
            stop();
        }
    }

    private void startLoop() throws IOException {
        while (!Thread.interrupted()) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            for (SelectionKey key : keys) {
                dispatch(key);
            }
            keys.clear();
        }
    }

    private void dispatch(SelectionKey key) {
        Runnable r = (Runnable) key.attachment();
        if (r != null) {
            r.run();
        }
    }

    private void stop() {
        try {
            serverSocket.close();
            selector.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error during stopping server. Ignoring", e);
        }
    }
}
