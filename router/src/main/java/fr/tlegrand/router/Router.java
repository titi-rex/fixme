/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package fr.tlegrand.router;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Clock;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author reina
 */
public class Router {

    private Map<Integer, Client> clients;

    private ServerSocketChannel brokersSocket;
    private ServerSocketChannel marketsSocket;
    private Selector selector;

    private final String host = "127.0.0.1";
    private final int portBrokers = 5000;
    private final int portMarkets = 5001;

    public static void main(String[] args) {
        Router router = new Router();
        try {
            router.init();
            router.run();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            router.clean();
        }
    }

    public Router() {
        clients = new HashMap<>();
//        idCount = Integer.valueOf(0);
    }

    public void init() throws IOException {
        System.out.println("Starting Router...");

        selector = Selector.open();
        brokersSocket = InitServerSocket(portBrokers);
        marketsSocket = InitServerSocket(portMarkets);

        System.out.println("Router initialized! Ready to accept clients");
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
/*
    1) get serverchannel
    2) accept and get clientchannel
    3) create new client
    4) register new client inside index
    5) register new into selector
                 */
                if (key.isAcceptable()) {
                    System.out.println("accepting new client");
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = server.accept();

                    Client newClient = new Client();
                    clients.put(newClient.getId(), newClient);

                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, newClient);

                    System.out.println("Client " + newClient.getId() + " added!");

                    iter.remove();
                    continue;
                }
//                if read -> -1 -> discard client
//                read message -> validate it -> buffer it
                if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer dst = ByteBuffer.allocate(1024);
                    int nRead = client.read(dst);
                    if (nRead <= 0) {
                        System.out.println("Client " + ((Client)key.attachment()).getId() + " disconneted.");
                        key.cancel();
                        client.close();
//                        remove from map
                        iter.remove();
                        continue;
                    } else {
                        System.out.println("Echo: " + dst);
                    }

                }
//                if messages are buffered, forward them
                if (key.isWritable()) {

                }
                iter.remove();
            }

        }
    }

    public void clean() {
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
