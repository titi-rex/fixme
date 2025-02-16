/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Singleton.java to edit this template
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
