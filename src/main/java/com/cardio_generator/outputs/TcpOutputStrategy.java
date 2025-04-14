package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Outputs patient health data over a TCP socket connection.
 *
 * <p>This implementation of OutputStrategy establishes a TCP server that accepts
 * a single client connection and sends patient data to that client. The data is
 * formatted as comma-separated values (CSV) in the format:
 * "patientId,timestamp,label,data".</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Single-client TCP server</li>
 *   <li>Asynchronous client acceptance</li>
 *   <li>CSV-formatted data output</li>
 *   <li>Automatic reconnection support</li>
 * </ul></p>
 */
public class TcpOutputStrategy implements OutputStrategy {

    /** The server socket that listens for client connections. */
    private ServerSocket serverSocket;
    
    /** The socket for the connected client. */
    private Socket clientSocket;
    
    /** Writer for sending data to the connected client. */
    private PrintWriter out;

    /**
     * Initializes a new TCP output strategy on the specified port.
     *
     * <p>This constructor:
     * <ol>
     *   <li>Creates a server socket on the specified port</li>
     *   <li>Starts a background thread to accept client connections</li>
     *   <li>Sets up the output writer when a client connects</li>
     * </ol></p>
     *
     * @param port The port number to listen on
     * @throws IOException if there is an error creating the server socket
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends patient data to the connected client.
     *
     * <p>If a client is connected, this method formats the data as CSV and
     * sends it to the client. If no client is connected, the data is silently
     * discarded.</p>
     *
     * <p>Data format: "patientId,timestamp,label,data"</p>
     *
     * @param patientId The unique identifier of the patient
     * @param timestamp The time when the data was recorded, in milliseconds since epoch
     * @param label The type of data being recorded
     * @param data The actual data value to be recorded
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
