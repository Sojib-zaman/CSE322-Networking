package ServerSide;

import Profile.Message;
import Profile.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Worker extends Thread {
    Socket socket;
    Socket filesocket ;
    ObjectInputStream inputStream = null ;
    ObjectOutputStream outputStream = null ;
    ObjectInputStream inputStreamfile = null ;
    ObjectOutputStream outputStreamfile= null  ;
    HashMap<Integer,String>Commands=new HashMap<>();
    User user;
    ServerFileProcess sfp ;
    String input ;
    String[] tokened_command ;
    String filename;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_GREEN = "\u001B[32m";

    public HashMap<Integer, String> getCommands() {
        return Commands;
    }



    public Worker(Socket socket, Socket filesocket , ObjectOutputStream out, ObjectInputStream in ,  ObjectOutputStream fout  , ObjectInputStream
                   fin ) throws IOException {
        this.socket = socket;
        this.filesocket=filesocket;
        this.inputStreamfile=fin;
        this.outputStream=out;
        this.outputStreamfile=fout;
        this.inputStream=in ;
        setupcommands();

    }

    private void setupcommands() {
        Commands.put(1,"Upload") ;
        Commands.put(2,"See Entire User base ") ;
        Commands.put(3,"Show a particular user files") ;
        Commands.put(4,"Show my files") ;
        Commands.put(5,"Request a file") ;
        Commands.put(6,"Download") ;
        Commands.put(7,"Show unread Messages") ;
        Commands.put(8,"Logout") ;


    }


    public int userHistory(String username)
    {

        for(User user : Server.clientlist)
        {

            if(user.getUsername().equals(username))
            {
                if(user.getStatus().equalsIgnoreCase("Online")) return 1 ;
                else return 2;
            }
        }

        return 0 ;
    }


    boolean add_user() throws IOException, ClassNotFoundException {

        this.user=null;
        String username = (String) inputStream.readObject() ;
        //1 means currently online , 2 means previously joined and then offline , 0 means new user logged in
        int status = userHistory(username) ;
        if(status==1)
        {
            outputStream.writeObject("This user "+ username +" already logged in. " + ". The connection will be closed ");
            System.out.println(ANSI_RED+"The user is already logged in ."+ANSI_RESET);
            return false;
        }
        else if(status==2)
        {
            // may need to remove previous user1
            System.out.println(ANSI_YELLOW+"Welcome again "+username+ANSI_RESET);
            User user1=find_user(username) ;
            this.user=user1;
            user.setStatus(true);
            outputStream.writeObject("SUCCESS");

        }
        else
        {
            System.out.println(ANSI_BLUE+"Welcome new user "+username+ANSI_RESET) ;
            this.user=new User(username);
            Server.adduser(user);
            outputStream.writeObject("SUCCESS");


        }

        System.out.println("Succesfully logged in : "+username);
        sfp=new ServerFileProcess(inputStreamfile,outputStreamfile,user) ;
        return true;



    }

    private User find_user(String username) {
        for(User clientProfile : Server.clientlist) {
            if (clientProfile.getUsername().equals(username)) {
                return clientProfile;
            }
        }
        return null;
    }
    public void sendalluserlist() throws IOException {
        //System.out.println("Sending the complete user list");
        ArrayList<String>userlist = new ArrayList<>();
        userlist.add("USER_NAME    "+"          STATUS");
        for(User user : Server.clientlist)
        {
            if(user.getStatus().equalsIgnoreCase("Online"))
                 userlist.add(ANSI_GREEN+" * "+user.getUsername()+" ---|--- "+user.getStatus()+ANSI_RESET);
            else
                userlist.add(ANSI_YELLOW+"   "+user.getUsername()+" ---|--- "+user.getStatus()+ANSI_RESET);
        }

       // System.out.println("done 33");
        outputStream.writeObject(userlist);
    }


    public void sendmyfiles() throws IOException {
        String files = ANSI_RED+"Public files:\n"+ANSI_RESET;
        files += String.format("%-10s %-10s %s\n", "FileID", "Filename", "Access");
        for(String ID : user.getallfileID(0))
            files += String.format("%-10s %-10s %s\n", ID, new File(Server.FileMap.get(ID)).getName(), "Public");
        files += ANSI_RED+"Private files:\n"+ANSI_RESET;
        files += String.format("%-10s %-10s %s\n", "FileID", "Filename", "Access");
        for(String ID : user.getallfileID(1))
            files += String.format("%-10s %-10s %s\n", ID, new File(Server.FileMap.get(ID)).getName(), "Private");
        outputStream.writeObject(files);
    }
    public void senduserfilelist(String username) throws IOException {
        //System.out.println("Sending "+ user.getUsername()+" specific filenames about "+username);
        User user1=find_user(username);
        if(user1!=null)
        {
            String msg = ANSI_YELLOW+"Public files of " + username + ":\n"+ANSI_RESET;
            msg += String.format("%-10s %-10s\n", "FileID", "FileName");
            for (String ID : user1.getallfileID(0)) {
                String filename = new File(Server.FileMap.get(ID)).getName();
                msg += String.format("%-10s %-10s\n", ID, filename);
            }
            outputStream.writeObject(msg);
            return;
        }
       // System.out.println("null");
        outputStream.writeObject("Can't find user "+username);


    }

    public ArrayList<String> check_public(String f_id) {
        ArrayList<String>ret = new ArrayList<>();
        for (User user : Server.clientlist) {
            ArrayList<String> publicFileID = user.getallfileID(0);
            if (publicFileID.contains(f_id)) {

                ret.add(user.getUsername());
            }
        }
        return ret;
    }




    public void sendfile(String f_id , String owner) throws IOException
    {
        System.out.println(f_id);
        System.out.println(owner);
        if(Server.FileMap.containsKey(f_id) && check_public(f_id).contains(owner))
        {
            //System.out.println("Preparing to send : "+Server.FileMap.get(f_id));
            sfp.setFilename(Server.FileMap.get(f_id));
            sfp.setMode("UPLOAD");
            //System.out.println("going to server file process");
            Thread upload = new Thread(sfp) ;
            upload.start();
            try {
                upload.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }
        System.out.println("File not found");
        outputStreamfile.writeObject("NOT FOUND");
    }

    public void run()
    {
       // System.out.println("Currently in worker run thread");
        File[] files ;
        try {

            if(add_user())
            {
                outputStream.writeObject(Commands);
                while (user.getStatus().equalsIgnoreCase("online"))
                {

                     input = (String)inputStream.readObject();
                   // System.out.print("CLIENT INPUT : ");
                    // System.out.println(input);
                    tokened_command = input.split("#") ;



                    if(tokened_command[0].equals("UPLOAD"))
                    {
                        outputStreamfile.writeObject("Preparing to upload ");
                        this.sfp.setcommandstoken(tokened_command);
                        sfp.setRequestID(Integer.parseInt(tokened_command[4]));
                        sfp.setMode("DOWNLOAD"); // client upload so server download
                        Thread x = new Thread(sfp);
                        x.start();
                        try {
                            x.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    else if(input.equals("2"))
                     sendalluserlist();
                    else if(input.equals("3"))
                    {

                        outputStream.writeObject("From Worker : Enter username");
                       String username = (String) inputStream.readObject();
                        senduserfilelist(username);

                    }
                    else if(input.equals("4"))
                     sendmyfiles();


                    else if(tokened_command[0].equals("REQUEST"))
                    {

                        Server.file_id += 1 ;
                        String req_msg = ANSI_YELLOW+" Request id : "+ANSI_RESET+Server.file_id ;
                        req_msg+=ANSI_YELLOW+" file description : "+ANSI_RESET+ tokened_command[1] ;
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        for(User u : Server.clientlist)
                        {
                            if(!u.getUsername().equals(user.getUsername())) {
                                Message message = new Message(this.user, u, req_msg, formatter.format(new Date()));
                                u.addMessage(message);
                            }
                        }
                        //System.out.println("Inside request");

                        Server.ReqMap.put(Server.file_id , user) ;
                       // outputStream.writeObject("Your request ID : " + Server.file_id);


                    }
                    else if(tokened_command[0].equals("DOWNLOAD"))
                    {
                       // System.out.println("Worker is redirecting to sendfile");
                        sendfile(tokened_command[1] , tokened_command[2]);

                    }
                    else if(input.equals("7"))
                    {
                        //System.out.println("input is 7");
                        ArrayList<String>unread=user.ShowUnreadMessage();
                        if(unread.size()==0)outputStream.writeObject(unread);
                        else
                        {
                            user.readallunreadmessage();
                            outputStream.writeObject(unread);
                        }

                    }
                    else if(input.equals("8"))
                    {
                        System.out.println("User : " + this.user.getUsername() + " got disconnected.");
                        user.setStatus(false);
                        outputStream.writeObject("See you soon "+user.getUsername());
                        socket.close();
                        filesocket.close();
                    }
                    else
                    {
                        System.out.println("Unrecognized input");
                        outputStream.writeObject("Valid input please ");
                    }





                }
            }
            else
            {
                socket.close();
                filesocket.close();
            }

        }
        catch (Exception e)
        {
            System.out.println("User : " + this.user.getUsername() + " got disconnected.");
            this.user.setStatus(false);
            if(user.unfinished==1)
            {
                System.gc();
                try
                {
                    inputStreamfile.close();
                    outputStream.close();
                    inputStream.close();
                    outputStreamfile.close();
                    socket.close();
                    filesocket.close();
                }
                catch (Exception c)
                {
                    System.out.println("pri");
                }



            }
        }
    }
}
