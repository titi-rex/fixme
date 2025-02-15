/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.tlegrand.router;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author reina
 */
@Getter @Setter public class Client {
    static public int idCount;
    private int id;

    public List<String> waitingMessage;

    public Client() {
        this.id = idCount++;
    }
}

/*

map id -> client ()

keys -> channels 
    -> attachement -> id + readBuffer + waiting message  == client
                 
read -> disconnect
     -> read -> store in buffer
     -> if read end -> check msg and forward (buffer)
            THR ==> validate checksum 
                            -> look target (use map) (from id to client(or chanatcj)
                                -> add a waiting message 
                                // add waiting message error to client
     -> if write -> if message waiting -> write

*/