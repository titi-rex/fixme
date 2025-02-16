/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.tlegrand.router;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author reina
 */
@Getter @Setter public class FixHandler {

    private ByteBuffer readBuffer;
    private int bufferIdx;
    
    public FixHandler() {
        readBuffer = ByteBuffer.allocate(1024);
    }
    
}
/*

end read if : checksum == tag 10



*/