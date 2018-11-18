package edu.coursera.distributed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs A proxy filesystem to serve files from. See the PCDPFilesystem
     *           class for more detailed documentation of its usage.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket socket, final PCDPFilesystem fs)
            throws IOException {
        while (true) {
            Socket s = socket.accept();

            InputStream input = s.getInputStream();
            final String contents = readInput(input, fs);

            OutputStream output = s.getOutputStream();
            writeOutput(contents, output);
        }
    }

    /*
     * Using Socket.getInputStream(), parse the received HTTP
     * packet. In particular, we are interested in confirming this
     * message is a GET and parsing out the path to the file we are
     * GETing.
     * the first line of the received packet will look something like:
     *
     *     GET /path/to/file HTTP/1.1
     */
    private String readInput(final InputStream input, final PCDPFilesystem fs) throws IOException {
        InputStreamReader reader = new InputStreamReader(input);
        BufferedReader buffer = new BufferedReader(reader);

        String line = buffer.readLine();
        assert line != null;
        assert line.startsWith("GET");
        final String path = line.split(" ")[1];
        final String contents = fs.readFile(new PCDPPath(path));
        return contents;
    }


    /*
     * Using the parsed path to the target file, construct an
     * HTTP reply and write it to Socket.getOutputStream(). If the file
     * exists, the HTTP reply should be formatted as follows:
     *
     *   HTTP/1.0 200 OK\r\n
     *   Server: FileServer\r\n
     *   \r\n
     *   FILE CONTENTS HERE\r\n
     *
     * If the specified file does not exist, you should return a reply
     * with an error code 404 Not Found. This reply should be formatted
     * as:
     *
     *   HTTP/1.0 404 Not Found\r\n
     *   Server: FileServer\r\n
     *   \r\n
     */
    private void writeOutput(final String contents, OutputStream output){
        PrintWriter printer = new PrintWriter(output);

        if(contents != null){
            printer.print("HTTP/1.0 200 OK\r\n");
            printer.print("Server: FileServer\r\n");
            printer.print("Content-Length: " + contents.length() + "\r\n");
            printer.print("\r\n");
            printer.print(contents);
            printer.print("\r\n");

            printer.flush();
        } else {
            printer.print("HTTP/1.0 404 Not Found\r\n");
            printer.print("Server: FileServer\r\n");
            printer.print("\r\n");
        }
        printer.close();
    }
}
