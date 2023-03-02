package io.geekya215.nyaru;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Set;

public class Worker extends Thread {
    private final Selector selector;

    public Worker() throws IOException {
        this.selector = Selector.open();
    }

    public void register(SocketChannel socket) throws ClosedChannelException {
        SelectionKey key = socket.register(selector, SelectionKey.OP_READ);
        key.attach(new Processor(socket, key));
        selector.wakeup();
    }

    @Override
    public void run() {
        while (true) {
            try {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                for (SelectionKey key : keys) {
                    Processor processor = (Processor) key.attachment();
                    if (processor != null) {
                        processor.handle();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
