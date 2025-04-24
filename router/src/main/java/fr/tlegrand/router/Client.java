/*
 * The MIT License
 *
 * Copyright 2025 reina.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.tlegrand.router;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author reina
 */
@Getter
@Setter
public class Client implements Callable<Void> {

    static private final int DEFAULT_BUFFER_SIZE = 1024;

    static public int idCount;
    private final FXRouter router;
    private final Logger log;
    private int id;
    private final Socket socket;
    private final InputStreamReader reader;
    private final OutputStreamWriter writer;

    public Client(Socket socket, FXRouter router) throws IOException {
        this.socket = socket;
        this.router = router;
        reader = new InputStreamReader(socket.getInputStream(), FXRouter.CHARSET);
        writer = new OutputStreamWriter(socket.getOutputStream(), FXRouter.CHARSET);
        log = LoggerFactory.getLogger(this.getClass());
    }

//String.format("%06d", number);
// block until read
// parse n valid
//    find target
// submit write
    @Override
    public Void call() throws Exception {
        router.submit(() -> this.send("your id: " + this.id));
        while (true) {
            try {
                //   read message
                String message = read();
                if (message == null) {
                    log.debug("client disconnected");
                    return null;
                } else {
                    log.debug("client talking");
                }
                //  find dest
                int targetId = Integer.parseUnsignedInt(message.subSequence(0, 6), 0, 6, 10);
                Client target = Index.getInstance().find(targetId);
                //  forward message
                if (target != null) {
                    router.submit(() -> target.send(message));
                } else {
                    log.debug("target not found");
                }
            } catch (IOException e) {
                System.err.println("IO error -> close client : " + e);
            }
            log.debug("ned whille");
        }
    }

    public Void send(String message) throws IOException {
        this.writer.write(message);
        writer.flush();
        return null;
    }

//        thorw -> close client
    private String read() throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        InputStream in = socket.getInputStream();
        InputStreamReader reader = new InputStreamReader(in, StandardCharsets.ISO_8859_1);
        log.debug(this.id + "waiting on read...");
        int n = reader.read(buffer);
        if (n < 0) {
            return null;
        }
        return new String(buffer);
    }
}
