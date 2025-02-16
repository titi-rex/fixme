/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package fr.tlegrand.broker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author reina
 */
public class Broker {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        try (Socket socket = new Socket("localhost", 5000); 
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); 
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1)); 
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            String userInput;
            System.out.println("Connected to server. Type 'exit' to quit.");

            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                }
                String serverResponse = in.readLine();
                System.out.println("Server response: " + serverResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
