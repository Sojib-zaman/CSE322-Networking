package ClientSide;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class FileProcess implements Runnable{
    Socket socket ;
    ObjectInputStream inputStream;

    ObjectOutputStream outputStream ;
    String mode , privacy ;
    String download_path ;
    int request_id ;
    File file;
    String[] command_tokened;
    private String down_client;

    public FileProcess(Socket socket , ObjectInputStream in , ObjectOutputStream out)
    {
       this.socket=socket ;
       this.inputStream=in ;
       this.outputStream=out;

    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public void setDownload_path(String download_path) {
        this.download_path = download_path;
    }

    public void setRequest_id(int request_id) {
        this.request_id = request_id;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String UploadCommand()
    {
        return "UPLOAD#"+ this.privacy + "#"+ file.getName()+"#"+file.length()+"#"+request_id ;
    }
    public String DownloadCommand()
    {
        return "DOWNLOAD"+ "#"+ file.getName()+"#"+down_client;
    }

    public boolean sendingfile(File f , Integer chunksize) throws IOException, ClassNotFoundException

    {
        //System.out.println("In sending  file line 59 , fileprocess.java");
        //System.out.println(f.getName());
        //System.out.println(chunksize);


        this.socket.setSoTimeout(30000);
        FileInputStream fileInputStream = new FileInputStream(f);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        byte[] bytes;
        long finished=0;
        long filesize = f.length() ;
        int no_of_chunks = (int) Math.ceil((double)filesize/chunksize ) ;
        System.out.println("Total number of chunks : "+no_of_chunks);


        String ack = " " ;


        for(int i=0 ; i<no_of_chunks ;i++)
        {
            if (finished + chunksize <= filesize) finished += chunksize;
            else {
                // last er part which is less than chunksize
                chunksize = (int) (filesize - finished);
                finished = filesize;
            }
            bytes = new byte[chunksize];
            bufferedInputStream.read(bytes, 0, chunksize);
            outputStream.writeObject(bytes);
            outputStream.flush();
            try {
                ack=(String) inputStream.readObject();
                if(ack.equals("ACK")) {
                    System.out.println("Sent chunk "+i); // send next chunk if only successful acknowledge
                }
                else
                {
                    this.outputStream.flush();
                    bufferedInputStream.close();
                    return false;
                }

            } catch (Exception e)
            {
                System.out.println("Time out");
                outputStream.writeObject("TIME OUT");
                break;
            }
        }
        socket.setSoTimeout(0);
        outputStream.flush();
        bufferedInputStream.close();

        outputStream.writeObject("COMPLETE"); //sending complete message
        ack = (String) inputStream.readObject();
        if(ack.equals("COMPLETE"))
        {
            System.out.println(f.getName()+" with size "+f.length()+" bytes sending done");
            return true;
        }
        return false ;


    }
    public boolean uploadfile() throws IOException, ClassNotFoundException
    {


      //  System.out.println("In file process upload file");
        Integer chunksize = 400 ;


        System.out.println(inputStream.readObject());
       // System.out.println("krai");

        System.out.println(inputStream.readObject());

         chunksize =(Integer) inputStream.readObject();
        System.out.println("Chunk size "+ Integer.toString(chunksize));
        System.out.println("File ID : "+inputStream.readObject());

        //System.out.println("done");
        return sendingfile(file,chunksize);
        // this chunksize will be sent by server which is a random one

    }
    public boolean downloadfile()
    {
        //System.out.println("In file process download file line 140");
        try {
           // System.out.println("try ? ");



            String command = (String) inputStream.readObject(); // contains abc.txt#size#chunk
            System.out.println(command);
            if(command.equalsIgnoreCase("NOT FOUND")) {return  false;}

            command_tokened = command.split("#") ;

            String filename = command_tokened[0] ;
           // System.out.println("In cfp , filename : "+filename);
            int filesize = Integer.parseInt(command_tokened[1]) ;
            int chunk_size = Integer.parseInt(command_tokened[2]) ;
           // System.out.println(download_path+"/"+filename);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(download_path+"/"+filename)) ;
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            int no_of_chunks = (int)Math.ceil((double) filesize/chunk_size) ;
            System.out.println("Completing in "+no_of_chunks+" chunks");
            byte[] bytes = new byte[chunk_size] ;
            int read = 0 ;
            int total = 0 ;

            for(int i=1 ; i<=no_of_chunks ; i++)
            {
                bytes = (byte[]) inputStream.readObject() ;
                read = bytes.length ;
                total+=read ;
                bufferedOutputStream.write(bytes,0,read);
                bufferedOutputStream.flush();
            }
          //  System.out.println("file done " +total);
            bufferedOutputStream.close();
            String response =(String) inputStream.readObject() ;
            System.out.println("-> From Server : "+response);
           // System.out.println("Successful");
            return true;
        }
        catch (Exception e)
        {
            System.out.println(e+" in download");
            return false;
        }

    }

    @Override
    public void run() {
        if (mode.equals("UPLOAD")) {
            try {

                boolean success = uploadfile();
                if (!success) System.out.println("File upload failed");

            } catch (Exception e) {
                System.out.println("File upload failed");
            }

        } else if (mode.equals("DOWNLOAD"))
        {
            try {

                boolean success = downloadfile();
                if (!success) System.out.println("File download failed");
            } catch (Exception e) {
                System.out.println("File upload failed");
            }
        }

    }

    public void setdownuser(String downClientName) {
        this.down_client = downClientName;
    }
}
