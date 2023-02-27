package io.geekya215.nyaru;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private ServerSocketChannel serverChannel;
    private Selector selector;

    @Override
    public void run() {
        try {
            init();
            startLoop();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error occurred. Stopping server", e);
        } finally {
            stop();
        }
    }

    public void init() throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(9527));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        LOGGER.log(Level.INFO, "Server start at port " + 9527);
    }

    private void startLoop() throws IOException {
        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            for (SelectionKey key : keys) {
                dispatch(key);
            }
            keys.clear();
        }
    }

    private void dispatch(SelectionKey key) throws IOException {
        if (!key.isValid()) {
            return;
        }

        if (key.isAcceptable()) {
            accept();
        } else if (key.isReadable()) {
            read(key);
        } else if (key.isWritable()) {
            write(key);
        }
    }

    private void accept() throws IOException {
        LOGGER.log(Level.INFO, "Connection established");
        SocketChannel clientChannel = serverChannel.accept();

        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        SocketChannel clientChannel = (SocketChannel) key.channel();

        int readBytes;

        while ((readBytes = clientChannel.read(buffer)) > 0) {
        }

        if (readBytes < 0) {
            throw new IOException("End of input stream. Connection is closed by the client");
        }

        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void write(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        SocketChannel clientChannel = (SocketChannel) key.channel();

        buffer.put("Echo from server...".getBytes());
        buffer.flip();
        clientChannel.write(buffer);

        if (buffer.hasRemaining()) {
            key.interestOps(SelectionKey.OP_WRITE);
        } else {
            closeChannel(key);
        }
    }

    private void closeChannel(SelectionKey key) {
        key.cancel();

        SocketChannel channel = (SocketChannel) key.channel();

        try {
            channel.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error during closing channel: " + channel, e);
        }
    }

    private void stop() {
        try {
            serverChannel.close();
            selector.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error during stopping server. Ignoring", e);
        }
    }
}
