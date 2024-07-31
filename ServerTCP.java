// Importação de Bibliotecas
import java.io.*;
import java.util.*;
import java.net.*;

public class ServerTCP {
    public static void main(String[] args) throws IOException {
        // Define a porta do Servidor
        int port = 5678;

        // Abre um soquete (socket) do Servidor na porta específicada
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Obtém o endereço IP da máquina local
            InetAddress localhost = InetAddress.getLocalHost();
            // Imprime no console o endereço junto com a porta onde o Servidor está disponível
            System.out.println("Servidor disponível em: " + localhost + ":" + port);

            while (true) {
                // Espera até se conectar com um cliente e cria um soquete (socket) para essa conexão
                Socket clientSocket = serverSocket.accept();
                // Imprime no console o endereço IP e a porta do cliente conectado
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                // Criação de thread para lidar com mais de um cliente ao mesmo tempo
                Thread clientThread = new ClientHandlerThread(clientSocket);
                clientThread.start();
            }
        }
    }
}

class ClientHandlerThread extends Thread {
    private final Socket clientSocket;

    // Construtora do classe, recebe o socket que representa a conexão com o cliente
    public ClientHandlerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    //
    @Override
    public void run() {
        try {
            // Objetos para entrada e saída de dados para o cliente
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();

            // Loop para ler as linhas do cliente
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Requisição do cliente: " + request);

                // Verifica se a solicitação é um GET
                if (request.startsWith("GET")) {
                    String[] requestParts = request.split(" ");
                    String requestedFile = requestParts[1].substring(1); // Remove a barra inicial
                    String contentType = "text/plain"; // Tipo de conteúdo padrão

                    if (requestedFile.endsWith(".html") || requestedFile.endsWith(".htm")) {
                        contentType = "text/html";
                    }
                    else if (requestedFile.endsWith(".jpeg") || requestedFile.endsWith(".jpg")) {
                        contentType = "image/jpeg";
                    }
                    else if (requestedFile.endsWith(".gif")) {
                        contentType = "image/gif";
                    }
                    else {
                        contentType = "application/octet-stream";
                    }

                    // Criação de um arquivo com base no nome requisitado
                    File file = new File(requestedFile);

                    // Verifica se o arquivo existe
                    if (file.exists()) {
                        FileInputStream fileInputStream = new FileInputStream(file);

                        // Resposta HTTP com o status de "200 OK"
                        String response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + contentType + "\r\n" +
                                "Content-Length: " + file.length() + "\r\n" +
                                "Date: " + new Date() + "\r\n" +
                                "\r\n";

                        out.write(response.getBytes());

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }

                        fileInputStream.close();
                    } else {
                        // Resposta HTTP com o status de "404 Not Found"
                        String notFoundResponse = "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: 13\r\n" +
                                "Date: " + new Date() + "\r\n" +
                                "\r\n" +
                                "404 Not Found";

                        out.write(notFoundResponse.getBytes());
                    }
                }
            }
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
