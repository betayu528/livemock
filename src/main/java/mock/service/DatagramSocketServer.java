package mock.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/*Test service */
public class DatagramSocketServer {
    private final static Integer LEN = 1024;

    private DatagramChannel channel; 

    public DatagramSocketServer() {
        
        byte [] array = new byte[LEN];
        DatagramPacket packet = new DatagramPacket(array, LEN);
        try {
            DatagramSocket socket = new DatagramSocket(18888);
            while (true) {
                socket.receive(packet);
                String recvStr = new String(array);
                
            }
            
        } catch(IOException e) {
            System.err.println(e);
        }
    }
    public static void main(String[] args) {
        byte [] array = new byte[1024];
        String abc = new String("abcdef");
        System.out.println(abc.hashCode());
        System.out.println(array.length);
        StringBuilder builder = new StringBuilder("abc");
        System.out.println(builder.toString());
        builder.append(abc);
        System.out.println(builder.toString());
        
        System.out.println(array.getClass());
        System.out.println(new String(array));
        //ByteBuffer buffer = new ByteBuffer();

        ArrayList list = new ArrayList<Integer>();
        list.iterator();
        list.add(0, 555);
        LinkedList linkList = new LinkedList<Integer>();
        ArrayList arraylist = new ArrayList<Integer>();

        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("a", 1000);
        hashMap.put("g", 5);
        
    }
}
