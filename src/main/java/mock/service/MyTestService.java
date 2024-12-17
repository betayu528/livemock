package mock.service;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/*调试作用 */
public class MyTestService {
    public static void main(String[] args) throws FileNotFoundException {
        
        try {
            PrintWriter out = new PrintWriter("output.data");
            for (int i = 0; i < 100; ++i) {
                out.write("hello_" + i + "\n");
            }
            out.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader("output.data"));
            while (reader.ready()) {
                String line = reader.readLine();
                System.out.println(line);
            }
           
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e);
        }

        try {
            DataOutputStream output = new DataOutputStream(new FileOutputStream("test_data.data"));
            // byte [] array = new byte [100];
            // array[0] = 0;
            // output.write(null);

            DataInputStream input = new DataInputStream(new FileInputStream("test_data.dat"));
        } catch (FileNotFoundException e) {
            // TODO: handle exception
            System.out.println(e);
        } 

        String [] myIps = {"127.0.0.1", "192.168.0.113"};
        System.out.println(myIps[1]);
        ArrayList<Integer> arrays = new ArrayList<Integer>(2);
        Integer [] array = new Integer[100];
        array[0] = 20;
        array[1] = 21;
        //arrays[0] = 2;
        arrays.add(100);
        System.out.println(array[0]);
    }
}
