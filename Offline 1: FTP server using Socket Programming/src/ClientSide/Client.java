package ClientSide;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Client {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static File client_storage= new File("src/ClientSide/Storage");
    static Integer request_ID = 0  ;
    public Client() throws IOException, ClassNotFoundException, InterruptedException {

        Client.client_storage.mkdir();



        Socket socket = new Socket("localhost",6666) ;
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()) ;
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream()) ;


        Socket filesocket = new Socket("localhost",6667) ;
        ObjectOutputStream file_out = new ObjectOutputStream(filesocket.getOutputStream()) ;
        ObjectInputStream file_in = new ObjectInputStream(filesocket.getInputStream()) ;


        System.out.println("Waiting for connection .. ");




        out.writeObject("-> From #Client : New Connection Request ");
        //NOW ASK FOR USERNAME
        System.out.println(ANSI_GREEN+"-> From #Server : "+ in.readObject()+ANSI_RESET);
        Scanner scanner = new Scanner(System.in) ;
        String username = scanner.nextLine();
        ClientHelper clientHelper = new ClientHelper(socket,filesocket,out,in,file_out,file_in);
        int valid = 1 ;
        if(!clientHelper.login(username))
        {
            valid = 0 ;
        }



        if(valid==1)
        {
            HashMap<Integer,String> Commands=new HashMap<>();
            Commands=(HashMap<Integer, String>) in.readObject();
            show_commands(Commands);
            while (true)
            {
                System.out.println("----------------------");
                System.out.println(ANSI_BLUE+"Press enter if you need help"+ANSI_RESET);
                System.out.println(ANSI_RED+"Input : "+ANSI_RESET);
                String input = scanner.nextLine();
                // System.out.println("Input given in Client : "+input);
                if(input.length()>0)
                {
                    //System.out.println("not empty");


                    if(input.equalsIgnoreCase("1")) takeUploadinfo(clientHelper);
                    else if(input.equalsIgnoreCase("5"))takerequestinfo(clientHelper) ;
                    else if (input.equalsIgnoreCase("6")) takedownloadinfo(clientHelper);


                    clientHelper.setCurrent_command(input);
                    Thread x = new Thread(clientHelper) ;
                    x.start();
                    try {
                        x.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else show_commands(Commands);
                if(input.equalsIgnoreCase("8"))break;


            }


        }

    }

    private void takerequestinfo(ClientHelper clientHelper) {
        Scanner sc = new Scanner(System.in) ;
        System.out.print(ANSI_YELLOW+"Enter a short description of your file : "+ANSI_RESET);
        String file_description=sc.nextLine() ;
        clientHelper.setrequest(file_description) ;
    }

    private void takedownloadinfo(ClientHelper clientHelper) {
        Scanner sc = new Scanner(System.in) ;
        System.out.println(ANSI_YELLOW+"Enter user name : "+ANSI_RESET);
        String user_name = sc.nextLine();
        System.out.println(ANSI_YELLOW+"Enter one of any public files ID : "+ANSI_RESET);
        String F_id = sc.nextLine();
        clientHelper.setDown_file(user_name,F_id);
    }

    private void takeUploadinfo(ClientHelper clientHelper) {
        Scanner sc = new Scanner(System.in) ;
        System.out.print(ANSI_BLUE+"Enter File Privacy : "+ANSI_RESET);
        String privacy=sc.nextLine();
        System.out.print(ANSI_BLUE+"Enter File location : "+ANSI_RESET);
        String filename=sc.nextLine() ;
        System.out.print(ANSI_BLUE+"Enter request ID if needed : "+ANSI_RESET);
        String req_id=sc.nextLine() ;
        if(req_id.equals("")) req_id="-1";
        else
        {
            if(privacy.equalsIgnoreCase("Private"))
                System.out.println("You are uploading to fulfill a request. File privacy changed to public");
            privacy="Public";
        }
        clientHelper.setuploadinfo(privacy,filename,Integer.parseInt(req_id));
    }

    void show_commands(HashMap<Integer,String> Commands)
    {
        for(Integer i = 1 ; i<9 ; i++)
            System.out.println(i+" ---> " +Commands.get(i));

    }
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


    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Client client = new Client();
    }
}
