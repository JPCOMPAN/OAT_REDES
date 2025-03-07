import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class cliente {
    private static final String SERVIDOR_IP = "localhost";
    private static final int PORTA = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVIDOR_IP, PORTA);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Digite o nome de usuário: ");
            String usuario = scanner.nextLine();
            System.out.print("Digite a senha: ");
            String senha = scanner.nextLine();

            output.writeObject(usuario);
            output.writeObject(senha);

            String resposta = (String) input.readObject();
            if (resposta.equals("LOGIN_SUCESSO")) {
                System.out.println("Login bem-sucedido!");
                while (true) {
                    System.out.println("Escolha uma opção: ENVIAR, BAIXAR, SAIR");
                    String comando = scanner.nextLine();
                    output.writeObject(comando);

                    if (comando.equals("ENVIAR")) {
                        System.out.print("Digite o caminho do arquivo para enviar: ");
                        String caminhoArquivo = scanner.nextLine();
                        File arquivo = new File(caminhoArquivo);
                        if (arquivo.exists()) {
                            output.writeObject(arquivo.getName());
                            output.writeObject(arquivo.length());
                            try (FileInputStream fileInput = new FileInputStream(arquivo)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = fileInput.read(buffer)) > 0) {
                                    output.write(buffer, 0, bytesRead);
                                }
                            }
                            System.out.println("Arquivo enviado com sucesso.");
                        } else {
                            System.out.println("Arquivo não encontrado.");
                        }
                    } else if (comando.equals("BAIXAR")) {
                        System.out.print("Digite o nome do arquivo para baixar: ");
                        String nomeArquivo = scanner.nextLine();
                        output.writeObject(nomeArquivo);
                        String respostaArquivo = (String) input.readObject();
                        if (respostaArquivo.equals("ARQUIVO_ENCONTRADO")) {
                            long tamanhoArquivo = (long) input.readObject();
                            System.out.print("Digite o caminho para salvar o arquivo: ");
                            String caminhoSalvar = scanner.nextLine();
                            try (FileOutputStream fileOutput = new FileOutputStream(caminhoSalvar)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                long totalBytesRead = 0;
                                while (totalBytesRead < tamanhoArquivo) {
                                    bytesRead = input.read(buffer);
                                    fileOutput.write(buffer, 0, bytesRead);
                                    totalBytesRead += bytesRead;
                                }
                            }
                            System.out.println("Arquivo baixado com sucesso.");
                        } else {
                            System.out.println("Arquivo não encontrado no servidor.");
                        }
                    } else if (comando.equals("SAIR")) {
                        break;
                    }
                }
            } else {
                System.out.println("Login falhou. Usuário ou senha incorretos.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
