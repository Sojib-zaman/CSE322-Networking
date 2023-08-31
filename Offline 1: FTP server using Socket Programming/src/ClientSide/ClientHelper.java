package ClientSide;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientHelper extends Thread  {
    Socket socket;
    Socket filesocket ;
    ObjectInputStream inputStream ;
    ObjectOutputStream outputStream ;
    ObjectInputStream inputStreamfile ;
    ObjectOutputStream outputStreamfile ;
    String server_respone;
    ArrayList<String> array_server_response = new ArrayList<>();
    String current_command ;
    File Storage ;
    FileProcess cfp ;
    private String up_priv , up_file;

    private String down_file_id;
    private String req_description;
    Integer Req_id ;
    private String down_client_name;

    public void setCurrent_command(String s)
    {
        current_command=s ;
    }

    public ClientHelper(Socket socket, Socket filesocket , ObjectOutputStream out, ObjectInputStream in ,  ObjectOutputStream fout  , ObjectInputStream
            fin ) throws IOException, ClassNotFoundException {
        this.socket = socket;
        this.filesocket=filesocket;
        this.inputStreamfile=fin;
        this.outputStream=out;
        this.outputStreamfile=fout;
        this.inputStream=in ;
        cfp=new FileProcess(filesocket,inputStreamfile,outputStreamfile);


    }
    public boolean login(String username) throws IOException, ClassNotFoundException {
        // Client sending the username to the server
        outputStream.writeObject(username);
        server_respone = (String) inputStream.readObject();


        System.out.println("-> From #Server : "+server_respone);
        if(server_respone.equalsIgnoreCase("SUCCESS"))
        {
            System.out.println("Logged in");
            set_storage(username) ;
            cfp.setDownload_path(Storage.getAbsolutePath());
            return true;
        }
        else
        {
            System.out.println("Log in failed");
            return  false ;
        }

    }

    private void set_storage(String username) {

        this.Storage=new File(Client.client_storage.getAbsolutePath()+'/'+username) ;
        this.Storage.mkdir();
    }

    public void getspecuser(String input) throws IOException, ClassNotFoundException
    {
       // System.out.println(input);
        outputStream.writeObject(input); //writes 3
        //worker asks for username
        System.out.println(inputStream.readObject());

        Scanner x = new Scanner(System.in) ;
        String name = x.nextLine();
        //System.out.println("Name is "+ name);
        outputStream.writeObject(name); //send name
        server_respone= (String) inputStream.readObject();
        System.out.println("-> From #Server : ");
        System.out.println(server_respone);

    }
    public void getuserlist(String input) throws IOException, ClassNotFoundException
    {
        outputStream.writeObject(input);
        array_server_response=(ArrayList<String>) inputStream.readObject();
        System.out.println("-> From #Server : ");
        if(array_server_response.size()!=0)
        {
            for(int i=0 ; i<array_server_response.size();i++)
                System.out.println(array_server_response.get(i));
            array_server_response.clear();
        }
        else
        {
            System.out.println(" ");
        }


    }
    public void downloadfile(String down_client_name,String down_file) throws IOException {
       // Sending the task to user ( that I want to download )
       // System.out.println("Downaload file name in client helper 112 : "+down_file);
        cfp.setMode("DOWNLOAD");
        cfp.setdownuser(down_client_name);
        cfp.setFile(new File(down_file));
        outputStream.writeObject(cfp.DownloadCommand());

        Thread download = new Thread(cfp) ;
        download.start();
        try {
            download.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       // System.out.println("done");
    }
    public void uploadfile(String privacy , Integer reqID , String filename) throws IOException {
       cfp.setMode("UPLOAD");
       cfp.setFile(new File(filename));
       cfp.setPrivacy(privacy);
       cfp.setRequest_id(reqID);

        //System.out.println("Going to write : "+cfp.UploadCommand());
        outputStream.writeObject(cfp.UploadCommand());

       Thread upload = new Thread(cfp) ;
       upload.start();
        try {
            upload.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       // System.out.println("done");
    }
    public void requestfile(String file_description) throws IOException {
        outputStream.writeObject("REQUEST#"+file_description);
    }
    @Override
    public void run() {
        //System.out.println("From ClientHelper,the current command is : " + current_command);
        try
        {

             if(current_command.equalsIgnoreCase("1")) uploadfile(up_priv , Req_id , up_file);
            else if(current_command.equalsIgnoreCase("2"))getuserlist("2");

            else if(current_command.equalsIgnoreCase("3"))
            {
                //System.out.println("Client Helper Line 124 "+current_command);
                getspecuser(current_command);
            }
            else if(current_command.equalsIgnoreCase("4")) seemyfiles("4");
            else if(current_command.equalsIgnoreCase("5")) requestfile(req_description);
             else if(current_command.equalsIgnoreCase("6")) downloadfile(down_client_name,down_file_id);
             else if(current_command.equalsIgnoreCase("7")) showmessage("7") ;
            else if(current_command.equalsIgnoreCase("8"))logout("8");

        }
        catch (Exception e)
        {
            System.out.println("An error occurred in client helper");
        }


    }

    private void showmessage(String input) throws IOException, ClassNotFoundException {
        outputStream.writeObject(input);
        array_server_response=(ArrayList<String>) inputStream.readObject();
        System.out.println("-> Your unread Messages : ");
        if(array_server_response.size()!=0)
        {
            for(int i=0 ; i<array_server_response.size();i++)
                System.out.println(array_server_response.get(i));
            array_server_response.clear();
        }
        else
        {
            System.out.println(" ");
        }
    }

    private void seemyfiles(String input) throws IOException, ClassNotFoundException {
       //System.out.println("Inside see my files ");
        outputStream.writeObject(input);
        server_respone=(String) inputStream.readObject();
        System.out.println("-> From #Server : ");
        System.out.println(server_respone);
    }

    private void logout(String number) throws IOException, ClassNotFoundException {
       // System.out.println("clienthelp logout");
        outputStream.writeObject(number);
        System.out.println(inputStream.readObject());
        this.socket.close();
        this.filesocket.close();
    }

    public void setuploadinfo(String privacy, String filename , int req_id) {
        this.up_priv = privacy ;
        this.up_file = filename;
        this.Req_id = req_id ;
    }
    public void setDown_file(String clientname , String file_ID)
    {
        this.down_client_name = clientname ;
        this.down_file_id = file_ID;
    }

    public void setrequest(String fileDescription) {
        this.req_description = fileDescription;
    }
}
