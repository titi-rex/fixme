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
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author reina
 */
public class Router {

    static public final Charset CHARSET = StandardCharsets.ISO_8859_1;
    private final Logger log;

    private final Index index;
    private final ExecutorService executor;
    private final CompletionService<Void> completionService;

    public Router() {
        index = Index.getInstance();
        executor = Executors.newVirtualThreadPerTaskExecutor();
        completionService = new ExecutorCompletionService<>(executor);
        log = LoggerFactory.getLogger(this.getClass());
    }

    public static void main(String[] args) {
        Router router = new Router();
        router.launch();
    }

    public void launch() {
        try {
            this.start();
            this.loop();
        } catch (FatalException e) {
            log.error("fatal: " + e);
            this.close();
        } finally {
            log.info("router closed");
        }
    }

    public void submit(Callable task) {
        completionService.submit(task);
    }

    public void close() {
        log.info("shutdown");
        executor.shutdown();
        try {
            if (executor.awaitTermination(10, TimeUnit.SECONDS) == false) {
                executor.shutdownNow();
                if (executor.awaitTermination(10, TimeUnit.SECONDS) == false) {
                    log.error("can't terminate task");
                }
            }
            log.debug("shutdown completed");
        } catch (InterruptedException e) {
            log.debug("interrupted while waiting for shutdown");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void start() throws FatalException {
        try {
            log.info("start router");
            this.submit(() -> listen(5000, executor));
            this.submit(() -> listen(5001, executor));
            throw new RejectedExecutionException("test");
        } catch (RejectedExecutionException e) {
            log.error(e.toString());
            throw new FatalException("fatal");
        }
    }

    private Void listen(int port, ExecutorService exe) {
        try (ServerSocket socket = new ServerSocket(port)) {
            log.info("listening on port: " + port);
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
        log.debug("loop");
        while (true) {
            try {
                // wake up when task client finish and remove client
                Future future = completionService.take();
                future.get();

            } catch (Exception e) {
                log.warn("catch : " + e);
            }
        }
    }

}
