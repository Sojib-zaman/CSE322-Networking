package ServerSide;

import ClientSide.Client;
import Profile.Message;
import Profile.User;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {


    //using static to access from worker without sending server object ♦
    static int MAX_BUFFER_SIZE=1000000000 ;
    static int MAX_CHUNK_SIZE= 10240;
    static int MIN_CHUNK_SIZE=10 ;
    static int USED_BUFFER=0 ;

    public static File ServerDirectory=new File("src/ServerSide/ServerDir") ;
    static ArrayList<User> clientlist = new ArrayList<>() ;

    static HashMap<String,String>FileMap=new HashMap<>() ;
    static HashMap<Integer,User>ReqMap=new HashMap<>() ;

    public static void adduser(User user) {
        clientlist.add(user);
    }
    public static Integer file_id = 0;


    void Delete_Old_directory(File directory)
    {
        if(directory.isDirectory())
        {
            File[] subdir = directory.listFiles() ;
            for(int i=0 ; i<subdir.length ; i++)
                Delete_Old_directory(subdir[i]);

        }
        directory.delete();
    }

    public Server() throws IOException, ClassNotFoundException {
        Delete_Old_directory(ServerDirectory);
        Delete_Old_directory(Client.client_storage);
        ServerDirectory.mkdir();

        ServerSocket welcomesocket = new ServerSocket(6666) ;
        ServerSocket welcomefilesocket = new ServerSocket(6667) ;


        while (true)
        {
            Socket socket = welcomesocket.accept() ;
           // System.out.println("Connection established");
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()) ;
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream()) ;

            Socket fileSocket = welcomefilesocket.accept();
          //  System.out.println("File Connection established");
            ObjectOutputStream file_out = new ObjectOutputStream(fileSocket.getOutputStream()) ;
            ObjectInputStream file_in = new ObjectInputStream(fileSocket.getInputStream()) ;

            System.out.println(in.readObject());

            out.writeObject("Please enter your username");



            //System.out.println("Sending the command list to client now");


            Thread worker = new Worker(socket,fileSocket,out,in,file_out,file_in) ;

            worker.start();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Server server = new Server() ;
    }
}
