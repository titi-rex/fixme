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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 *
 * @author reina
 */
public class Index {

    static private final int MIN_ID = 0;
    static private final int MAX_ID = 1_000_000;

    private Map<Integer, Client> internalIndex;
    private Random rand;

    private Index() {
        internalIndex = new HashMap<Integer, Client>();
        rand = new Random();

    }

    public static Index getInstance() {
        return IndexHolder.INSTANCE;
    }

    private static class IndexHolder {

        private static final Index INSTANCE = new Index();
    }

    public synchronized void register(Client client) {
        client.setId(newId());
        internalIndex.put(client.getId(), client);
    }

    public synchronized void unregister(int id) {
        internalIndex.remove(id);
    }

    public synchronized Client find(int id) {
        return internalIndex.get(id);
    }

    private int newId() {
        int id;

        do {
            id = rand.nextInt(MIN_ID, MAX_ID);
//            add a too much error mecanisme
        } while (internalIndex.containsKey(id));
        return id;
    }

}
