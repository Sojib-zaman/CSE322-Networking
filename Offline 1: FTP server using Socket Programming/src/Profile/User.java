package Profile;

import ClientSide.Client;
import ServerSide.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

public class User {
    String username ;
    boolean status ;
    int filecount ;
   public int unfinished=0;

    File file_location;
    File private_file_location;
    File public_file_location ;


    ArrayList<String>publicfileID , privatefileID;
    ArrayList<Message> inbox ;

    public User(String username )
    {
        // basic information
        this.username = username ;
        status=true ;
        filecount=0;


        // making file directories
       make_dir(Server.ServerDirectory);

        //initializing arraylists
        inbox= new ArrayList<>() ;
        privatefileID=new ArrayList<>();
        publicfileID=new ArrayList<>() ;
    }

    public File getPrivate_file_location() {
        return private_file_location;
    }

    public File getPublic_file_location() {
        return public_file_location;
    }

    void make_dir(File default_location)
    {

        file_location=new File(default_location.getAbsolutePath()+'/'+username);
        public_file_location=new File(file_location.getAbsolutePath()+"/public") ;
        private_file_location=new File(file_location.getAbsolutePath()+"/private") ;


        file_location.mkdir();
        public_file_location.mkdir();
        private_file_location.mkdir();
    }
    public String getUsername() {
        return username;
    }

    public void setUserID(String username) {
        this.username = username;
        System.out.println("Welcome user ID"+this.username);
    }
    public String getStatus() {
        String active = (status) ? "Online" : "Offline";
        return active;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getFilecount() {
        return filecount;
    }

    public void setFilecount(int filecount) {
        this.filecount = filecount;
    }

    public void setInbox(ArrayList<Message> inbox) {
        this.inbox = inbox;
    }

    public ArrayList ShowUnreadMessage()
    {
        System.out.println("For User : "+username);
        ArrayList<String>Unread = new ArrayList<>() ;
        for(Message m : inbox)
        {
            if(m.getStatus())
            {
                String s =m.getDate()+"\n" ;
                s+=m.getText()+ Client.ANSI_BLUE+ " -- sent by "+m.getSenderID().getUsername()+ Client.ANSI_RESET;
                Unread.add(s) ;

                System.out.println(s);
            }
        }
        return Unread;

    }
    public void readallunreadmessage()
    {
        ArrayList<Message>Unread = ShowUnreadMessage();
        for(Message m : inbox)
        {
            m.setStatus(false);
        }
    }
    public ArrayList<Message> getInbox() {
        return inbox;
    }
    public void  setmsg(ArrayList<Message>inb){inbox=inb;}
    public void addMessage(Message m) {
        inbox.add(m);
    }
    public void addpublicfileID(String FID)
    {
        publicfileID.add(FID) ;
    }
    public void addprivatefileID(String FID)
    {
        privatefileID.add(FID) ;
    }
    public ArrayList<String> getallfileID(int type)
    {
        //use type 0 for public , 1 for private and 2 sends all
        ArrayList<String>fs=new ArrayList<>() ;
        if(type==0) fs.addAll(publicfileID) ;
        else if(type==1) fs.addAll(privatefileID) ;
        else if(type==2) { fs.addAll(publicfileID) ;
            fs.addAll(privatefileID) ; }
        return fs ;
    }



}
