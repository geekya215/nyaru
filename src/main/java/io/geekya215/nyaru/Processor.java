package io.geekya215.nyaru;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Processor {
    private final SocketChannel socket;
    private final SelectionKey key;
    private final ByteBuffer inputBuffer = ByteBuffer.allocate(32);
    private final ByteBuffer outputBuffer = ByteBuffer.allocate(32);

    private final int READING = 0, WRITING = 1;
    private int state = READING;

    public Processor(SocketChannel socket, SelectionKey key) {
        this.socket = socket;
        this.key = key;
    }

    public void handle() {
        try {
            if (state == READING) read();
            else if (state == WRITING) write();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process() {
        outputBuffer.put("Echo from server...".getBytes());
    }

    private void read() throws IOException {
        socket.read(inputBuffer);
        if (inputIsComplete()) {
            inputBuffer.flip();
            process();
            inputBuffer.clear();
            state = WRITING;
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void write() throws IOException {
        outputBuffer.flip();
        socket.write(outputBuffer);
        if (outputIsComplete()) {
            outputBuffer.clear();
            key.cancel();
        }
    }

    private boolean inputIsComplete() {
        return inputBuffer.position() > 0;
    }

    private boolean outputIsComplete() {
        return !outputBuffer.hasRemaining();
    }
}
