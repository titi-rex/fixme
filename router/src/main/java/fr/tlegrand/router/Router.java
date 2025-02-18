/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package fr.tlegrand.router;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author reina
 */
public class Router {

    static public final Charset CHARSET = StandardCharsets.ISO_8859_1;
    private Logger log;

    private final Index index;
    private final ExecutorService executor;
    private final CompletionService<Void> completionService;

    public Router() {
        index = Index.getInstance();
        executor = Executors.newVirtualThreadPerTaskExecutor();
        completionService = new ExecutorCompletionService<>(executor);
        log = LoggerFactory.getLogger("router");
    }

    public static void main(String[] args) {
        Router router = new Router();
        router.launch();
    }

    public void launch() {
        this.start();
        this.loop();
    }

    public void submit(Callable task) {
        completionService.submit(task);
    }

    private void start() {
        try {
            log.info("start router");
            this.submit(() -> listen(5000, executor));
            this.submit(() -> listen(5001, executor));
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        }
    }

    private Void listen(int port, ExecutorService exe) {
        try (ServerSocket socket = new ServerSocket(port)) {
            log.info("launch TL on port: " + port);
            while (true) {
                Socket clientSocket = socket.accept();
                Client newClient = new Client(clientSocket, this);
                index.register(newClient);
                Future future = exe.submit(newClient);
                future.get();
            }
        } catch (Exception e) {
            log.error("Big error -> close router: " + e);
        }
        return null;
    }

    private void loop() {
        while (true) {
            try {
                //            wake up when task client finish and remove client
                Future future = completionService.take();
                future.get();

            } catch (Exception e) {
                log.warn("catch : " + e);
            }
        }
    }

}
