package edu.uob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

import edu.uob.DBCommands.DBCmd;
import edu.uob.DBDataStructure.DBPath;
import edu.uob.DBEngine.*;
import edu.uob.DBException.DBException;

/** This class implements the DB server. */
public final class DBServer {

  private static final char END_OF_TRANSMISSION = 4;
  private final DBPath rootBase;
  Parser parser = new Parser();

  public static void main(String[] args) throws IOException {
    new DBServer(Paths.get(".").toAbsolutePath().toFile()).blockingListenOn(8888);
  }

  public DBServer(File databaseDirectory) {
    this.rootBase = new DBPath(databaseDirectory);
  }


  /**
   <p>This method handles all incoming DB commands and carry out the corresponding actions.
   */
  public String handleCommand(String command) {
    DBCmd cmd;
    try {
      cmd = this.parser.parse(command, rootBase);
      cmd.interpretCMD();
      parser.updateDatabase();
    } catch (DBException | IOException e) {
      e.printStackTrace();
      return "[ERROR] There is an error message: " + e;
    }
    if(cmd.getCurrentTable()==null){
      return "[OK] Thanks for your message: " + command;
    }
    return "[OK] Thanks for your message: " + command + cmd.convertTableToString(cmd.getCurrentTable());
  }


  public DBPath getRootBase(){
    return rootBase;
  }


  //  === Methods below are there to facilitate server related operations. ===

  /**
   * Starts a *blocking* socket server listening for new connections. This method blocks until the
   * current thread is interrupted.
   *
   * @param portNumber The port to listen on.
   * @throws IOException If any IO related operation fails.
   */
  public void blockingListenOn(int portNumber) throws IOException {
    try (ServerSocket s = new ServerSocket(portNumber)) {
      System.out.println("Server listening on port " + portNumber);
      while (!Thread.interrupted()) {
        try {
          blockingHandleConnection(s);
        } catch (IOException e) {
          System.err.println("Server encountered a non-fatal IO error:");
          e.printStackTrace();
          System.err.println("Continuing...");
        }
      }
    }
  }

  /**
   * Handles an incoming connection from the socket server.
   *
   * @param serverSocket The client socket to read/write from.
   * @throws IOException If any IO related operation fails.
   */
  private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
    try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

      System.out.println("Connection established: " + serverSocket.getInetAddress());
      while (!Thread.interrupted()) {
        String incomingCommand = reader.readLine();
        System.out.println("Received message: " + incomingCommand);
        String result = handleCommand(incomingCommand);
        writer.write(result);
        writer.write("\n" + END_OF_TRANSMISSION + "\n");
        writer.flush();
      }
    }
  }
}
