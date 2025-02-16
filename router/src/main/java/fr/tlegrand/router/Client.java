/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.tlegrand.router;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Callable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author reina
 */
@Getter
@Setter
public class Client implements Callable<Void> {

    static private final int DEFAULT_BUFFER_SIZE = 1024;

    static public int idCount;
    private final Router router;
    private int id;
    private final Socket socket;
    private final InputStreamReader reader;
    private final OutputStreamWriter writer;

    public Client(Socket socket, Router router) throws IOException {
        this.socket = socket;
        this.router = router;
        reader = new InputStreamReader(socket.getInputStream(), Router.CHARSET);
        writer = new OutputStreamWriter(socket.getOutputStream(), Router.CHARSET);
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
                    System.out.println("client disconnected");
                    return null;
                } else {
                    System.out.println("client talking");
                }
                //  find dest
                int targetId = Integer.parseUnsignedInt(message.subSequence(0, 6), 0, 6, 10);
                Client target = Index.getInstance().find(targetId);
                //  forward message
                if (target != null) {
                    router.submit(() -> target.send(message));
                } else {
                    System.out.println("target not found");
                }
                throw new NumberFormatException();
            } catch (IOException e) {
                System.err.println("IO error -> close client : " + e);
            } 
            System.out.println("ned whille");
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
        System.out.println("waiting on read...");
        int n = reader.read(buffer);
        if (n < 0) {
            return null;
        }
        return new String(buffer);
    }
}
