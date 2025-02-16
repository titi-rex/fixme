/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package fr.tlegrand.router;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author reina
 */
public class Router {

    private Index index;
    private Map<String, Client> clients;
    final ExecutorService executor;

    static private final int DEFAULT_BUFFER_SIZE = 1024;
    static public final Charset CHARSET = StandardCharsets.ISO_8859_1;

    public Router() {
        index = Index.getInstance();
        clients = new HashMap<String, Client>();
        executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public static void main(String[] args) {
        Router router = new Router();
        router.start();
    }

    public void start() {
        try {
            System.out.println("start router");
            Future future = executor.submit(() -> listen(5000, executor));
//            executor.submit(() -> listen(5001, executor));
            future.get();
//            wake up when task client finish and remove client
        } catch (Exception e) {
            e.printStackTrace();
        }

//        wait until notify by TS1 TS2 to quit (error)
    }

    private void listen(int port, ExecutorService exe) {
        try (ServerSocket socket = new ServerSocket(port)) {
            System.out.println("launch TL on port: " + port);
            while (true) {
                Socket clientSocket = socket.accept();
                Client newClient = new Client(clientSocket, this);
                index.register(newClient);
                Future future = exe.submit(newClient);
                future.get();
            }
        } catch (Exception e) {
            System.err.println("Big error -> close router: " + e);
//            e.printStackTrace();
        }
    }

    public void submit(Callable task) {
        executor.submit(task);
    }

  


}
 /*

public class Router implements AutoCloseable {

    private Map<Integer, Client> clients;

    private ServerSocketChannel brokersSocket;
    private ServerSocketChannel marketsSocket;
    private Selector selector;
    private final ExecutorService executor;

    private final String host = "127.0.0.1";
    private final int portBrokers = 5000;
    private final int portMarkets = 5001;

    public static void main(String[] args) {
        try (Router router = new Router()) {
            router.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Router() throws IOException {
        System.out.println("Starting Router...");

        clients = new HashMap<>();
        executor = Executors.newVirtualThreadPerTaskExecutor();
        selector = Selector.open();
        brokersSocket = InitServerSocket(portBrokers);
        marketsSocket = InitServerSocket(portMarkets);

        System.out.println("Router initialized! Ready to accept clients");
    }

    public void close() throws IOException {
        clean();
    }

    public void run() throws IOException {
        while (true) {
            if (selector.select() < 0) {
                continue;
            }
            Set<SelectionKey> selectedKey = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKey.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();

//               accept and add new client

//    1) get serverchannel
//    2) accept and get clientchannel
//    3) create new client
//    4) register new client inside index
//    5) register new into selector
                
                if (key.isAcceptable()) {
                    executor.submit(() -> accept((ServerSocketChannel) key.channel(), this));
                    iter.remove();
                    continue;
                }
//                if read -> -1 -> discard client
//                read message -> validate it -> buffer it
//              executors.submit(read(schan, client)) -> 
//                    -> read -> buff
//                    -> parse rq
//                    -> mark message and mark OP_WRITE
                if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer dst = ByteBuffer.allocate(1024);
                    int nRead = client.read(dst);
                    if (nRead <= 0) {
                        System.out.println("Client " + ((Client) key.attachment()).getId() + " disconneted.");
                        key.cancel();
                        client.close();
//                        remove from map
                        iter.remove();
                        continue;
                    } else {
                        System.out.println("Echo: " + dst);
                    }

                }
//              executors.submit(socket, client)
                if (key.isWritable()) {

                }
                iter.remove();
            }

        }
    }

    void addClient(SocketChannel schan) throws IOException {
        synchronized (clients) {
            Client client = new Client(schan);
            clients.put(client.getId(), client);
            schan.configureBlocking(false);
            schan.register(selector, SelectionKey.OP_READ, client);
            System.out.println("Client " + client.getId() + " added!");
        }
    }

    void removeClient() {
        
    }
    
//create client
    private static void accept(ServerSocketChannel sschan, Router router) {
        try {
            router.addClient(sschan.accept());
        } catch (Exception e) {
            System.err.println("cant add client");
        }
    }

    private void clean() {
        System.err.println("cleanning");
        try {
            brokersSocket.close();
            marketsSocket.close();
            selector.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServerSocketChannel InitServerSocket(int port) throws IOException {
        System.out.print("Init socket on port: " + port);
        ServerSocketChannel socket = ServerSocketChannel.open();
        socket.configureBlocking(false);
        socket.bind(new InetSocketAddress(host, port));
        socket.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println(", done!");
        return socket;
    }
//    socket.cancel()
}


 */
