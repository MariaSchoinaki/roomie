package org.example.backend.domain;

import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ActionsForClients extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;
    private  static Chunk masterInput;
    private Chunk data;
    public ActionsForClients(Chunk data) {
        this.data = data;
//        try {
//            //out = new ObjectOutputStream(connection.getOutputStream());
//            //in = new ObjectInputStream(connection.getInputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void run() {
        //try {
          //masterInput = (Chunk) in.readObject();
            //System.out.println("oups here");
          //Chunk chunk = (Chunk) masterInput;
          String strObject = (String) data.getData();
          //JSONObject jsonObject = new JSONObject(strObject);
          System.out.println("hello from workers !this is what i have been sent :"+strObject);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                in.close();
//                out.close();
//            } catch (IOException ioException) {
//                ioException.printStackTrace();
//            }
       // }
    }
}
