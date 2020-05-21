package com.emis.http;
import java.io.*;
import java.net.Socket;

public class HttpClient
{
  public static void main(String [] args) throws Exception
  {
    try {
      Socket s = new Socket("localhost",8080);
      final Reader from_server = new InputStreamReader(s.getInputStream());
      PrintWriter to_server = new PrintWriter(s.getOutputStream());
      BufferedReader from_user = new BufferedReader ( new InputStreamReader(System.in));
      final PrintWriter to_user = new PrintWriter(System.out,true);

      Thread t = new Thread() {
        char [] buffer = new char [1024];
        int chars_read;
        public void run() {
        try {
          while(( chars_read = from_server.read(buffer)) != -1) {
            for(int i=0;i<chars_read;i++)
            {
              if( buffer[i]=='\n')
                to_user.println();
              else
                to_user.print(buffer[i]);
            }
            to_user.flush();
          }
        }catch (IOException e) { to_user.println(e); }

        to_user.println("connection closed by server");
        System.exit(0);
        }
      }; // end of thead

      t.setPriority( Thread.currentThread().getPriority()+1);
      t.start();

      String line;
      while(( line = from_user.readLine()) != null)
      {
        to_server.print(line+"\n");
        to_server.flush();
      }
      s.close();
      to_user.println("connection closed by client");
      System.exit(0);




    } catch (Exception e) {
      System.err.println(e);
    }
  }
}