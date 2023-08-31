package ServerSide;

import Profile.Message;
import Profile.User;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ServerFileProcess implements Runnable {
    ObjectOutputStream outputStream = null   ;
    ObjectInputStream inputStream= null  ;
    BufferedOutputStream bufferedOutputStream= null  ;
    User user ;
    String filename , fileID, privacy , mode ;
    String[] command_tokened;
    Integer filesize=0;
    Integer requestID;

    public  ServerFileProcess(ObjectInputStream in , ObjectOutputStream out , User user1)
    {
        this.inputStream = in ;
        this.outputStream = out ;
        this.user = user1  ;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setRequestID(Integer requestID) {
        this.requestID = requestID;
    }

    public void setcommandstoken(String[] cmd_tokenized){ this.command_tokened = cmd_tokenized; }

    public Integer random_chunk() throws IOException {
        File filepath ;
        filesize = Integer.valueOf(command_tokened[3]) ;
        if(command_tokened[1].equalsIgnoreCase("PUBLIC"))
        {
            //System.out.println("Random Chunk "+ "Public file");
            filepath=user.getPublic_file_location() ;
            privacy = "public" ;
        }
        else
        {
            //System.out.println("Random Chunk "+ "Private file");
            filepath=user.getPrivate_file_location() ;
            privacy = "private" ;
        }
        filename = new String(filepath.getAbsolutePath()+"\\"+command_tokened[2]) ;

       // System.out.println("Random Chunk "+ filename+" "+filesize);


        if(!OK_FILE(filesize))
        { try
                {
                    outputStream.writeObject("Not enough space");
                }
                catch (Exception e)
                {
                    System.out.println(e);
                }
        }
        else
        {
            outputStream.writeObject("File size ok");
            Server.USED_BUFFER+=filesize ;
        }

        int chunk_size= get_rand();
        if(command_tokened[4].equalsIgnoreCase("-1"))
        {
           Server.file_id += 1 ;
           fileID=String.valueOf(Server.file_id);
        }
        else
        {
            this.fileID = this.command_tokened[4];
        }
        //System.out.println("In random chunk , chunksize : "+chunk_size) ;
        outputStream.writeObject(chunk_size);
        outputStream.writeObject(fileID);
        return chunk_size;

    }

    private int get_rand() {
        Random random = new Random() ;
        int chunk = Server.MIN_CHUNK_SIZE + Math.abs(random.nextInt() % (Server.MAX_CHUNK_SIZE-Server.MIN_CHUNK_SIZE+1)) ;
        return chunk;
    }

    boolean receive_file(Integer chunk) throws IOException, ClassNotFoundException, InterruptedException {
         //System.out.println("In receive file sfp line 108");
         //System.out.println(filename);
         //System.out.println(chunk);
         int no_of_chunks = (int) Math.ceil((double)this.filesize / chunk);
         System.out.println("Total number of chunks : "+no_of_chunks);



        // System.out.println("Going to create bos");
        FileOutputStream fs = new FileOutputStream(filename) ;
        bufferedOutputStream = new BufferedOutputStream(fs) ;
         //System.out.println("Created bos successful");
        byte[] bytes = new byte[chunk] ;
        Object obj ;

        int read = 0 ;
        int total = 0 ;




        for(int i = 0 ; i<no_of_chunks ; i++)
        {
            obj = inputStream.readObject();
            if(obj.getClass()== String.class)
            {
                System.out.println("Client "+ "send string which indicates a time out");
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
                return  false;
            }
            bytes = (byte[]) obj;
            read = bytes.length; //current chunk
            total+=read; // total received
            bufferedOutputStream.write(bytes,0,read);
            bufferedOutputStream.flush();
            outputStream.writeObject("ACK");
        }
        bufferedOutputStream.close();
        fs.close();
        String complete = (String) inputStream.readObject();

        System.out.println("-> From #Client : "+complete);
        if(!complete.equalsIgnoreCase("complete"))
        {
            System.out.println("Not a completion message ");
            return false;
        }
        if(total==filesize)
        {
            System.out.println("File upload successful ");
            return true;
        }

        return false;

    }



    public boolean Download()
    {
        boolean success = false ;
       // System.out.println("IN SERVER SIDE DOWNLOAD FILE PREOCESS ");
        try {
            int chunk = random_chunk();
            success=receive_file(chunk);
            if(!success)
            {
                outputStream.writeObject("FAILURE");
                File file1=new File(filename) ;
                file1.delete();
            }
            else {
                outputStream.writeObject("COMPLETE");
                Server.FileMap.put(fileID,filename) ;

                if(privacy.equalsIgnoreCase("private")) user.addprivatefileID(fileID);
                else if(privacy.equalsIgnoreCase("public"))user.addpublicfileID(fileID);
                else return false;

                Server.USED_BUFFER+=filesize;
                handlerequest(requestID);

            }
        }
        catch (Exception e)
        {

            System.out.println(e);
        }
        return  success;
    }

    private void handlerequest(Integer requestID) {
        if(requestID!=-1) //it means the user is uploading to satisfy a request
        {
            if(Server.ReqMap.containsKey(requestID)) //someone made such req
            {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                User receiver = Server.ReqMap.get(requestID) ;
                receiver.addMessage(new Message(this.user , receiver , "Your request for file ID : "+requestID+" is fulfilled. Thank you. " , formatter.format(new Date() )));
            }
            else
            {
                // System.out.println("Uploaded normally , can't fulfill any request");
            }

        }
    }

    public boolean Upload()
    {

       // System.out.println(filename); // here filename has entire location
        try {
            File file = new File(filename);
            outputStream.writeObject(file.getName()+"#"+file.length()+"#"+Server.MAX_CHUNK_SIZE);
            FileInputStream fileInputStream = new FileInputStream(file) ;
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream) ;

            byte[] bytes;
            long finished=0;
            long filesize = file.length() ;
            int chunksize = Server.MAX_CHUNK_SIZE;
            int no_of_chunks = (int) Math.ceil((double)filesize/chunksize ) ;
            System.out.println("Total number of chunks : "+no_of_chunks);



            for(int i=0 ; i<no_of_chunks ;i++) {
                if (finished + chunksize <= filesize) finished += chunksize;
                else {
                    // last er part which is less than chunksize
                    chunksize = (int) (filesize - finished);
                    finished = filesize;
                }
                bytes = new byte[chunksize];
                bufferedInputStream.read(bytes, 0, chunksize);
                outputStream.writeObject(bytes);
            }
            outputStream.flush();
            bufferedInputStream.close();
            outputStream.writeObject(filesize+" bytes transfer from server to client done");
            System.out.println("Finished");
            return  true;
        }
        catch (Exception e)
        {
            System.out.println("Exception occured in Server File Process Upload");
            return false;
        }

    }
    public boolean OK_FILE(int filesize)
    {
        if(Server.USED_BUFFER+filesize<=Server.MAX_BUFFER_SIZE)return true ;
        return false;
    }

    @Override
    public void run() {
        //System.out.println("Inside Server side file processing");
        if (mode.equals("UPLOAD")) {
           // System.out.println("On my way to upload from server to client");
                boolean success = Upload();
                if (!success) System.out.println("File uploading failed");

        } else if (mode.equals("DOWNLOAD"))
        {
            //System.out.println("In server file download");
            boolean success = Download();
            if (!success)
            {
                System.out.println("File receiving failed");

               // File file1=new File(filename) ;
               // if(file1.exists()) System.out.println(file1.length());


                File tempFile=new File(filename) ;
                try {

                    outputStream.close();
                    inputStream.close();
                    this.bufferedOutputStream.close();
                    tempFile.delete();

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
               user.unfinished=1;
            }
        }
    }
}
