package io.geekya215.nyaru;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EventLoop implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(EventLoop.class.getName());

    private ServerSocketChannel serverSocket;
    private Selector selector;
    private static final int NUMS_OF_WORKER = Runtime.getRuntime().availableProcessors();
    private static final Worker[] workers = new Worker[NUMS_OF_WORKER];
    private int seq = 0;

    public void init() throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        for (int i = 0; i < NUMS_OF_WORKER; i++) {
            workers[i] = new Worker();
            workers[i].setName("worker" + i);
            workers[i].start();
        }
    }

    public void listen(int port) throws IOException {
        serverSocket.bind(new InetSocketAddress(port));
    }

    public void startLoop() throws IOException {
        while (true) {
            selector.select();

            Set<SelectionKey> keys = selector.selectedKeys();
            for (SelectionKey key : keys) {
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel socket = channel.accept();
                    socket.configureBlocking(false);
                    LOGGER.log(Level.INFO, "Connection established by " + socket.getRemoteAddress());
                    int next = seq++ % NUMS_OF_WORKER;
                    Worker worker = workers[next];
                    worker.register(socket);
                }
            }
            keys.clear();
        }
    }

    public void stop() {
        try {
            serverSocket.close();
            selector.close();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during stopping server. Ignoring", e);
        }
    }

    @Override
    public void run() {
        try {
            init();
            listen(9527);
            LOGGER.log(Level.INFO, "Server start at port " + 9527);
            startLoop();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error occurred. Stopping server", e);
        } finally {
            stop();
        }
    }
}
