import java.io.*;
import java.net.*;
import java.util.*;

public class cliente {
    private static final String servidor_ip = "localhost";
    private static final int porta = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(servidor_ip, porta);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Digite o nome de usuário: ");
            String usuario = scanner.nextLine();
            System.out.print("Digite a senha: ");
            String senha = scanner.nextLine();

            output.writeObject(usuario);
            output.writeObject(senha);
            output.flush();

            String resposta = (String) input.readObject();
            if (resposta.equals("LOGIN_SUCESSO")) {
                System.out.println("Login bem-sucedido!");
                while (true) {
                    System.out.println("Escolha uma opção: ENVIAR, BAIXAR, LISTAR, SAIR");
                    String comando = scanner.nextLine();
                    output.writeObject(comando);
                    output.flush();

                    if (comando.equals("ENVIAR")) {
                        System.out.print("Digite o caminho do arquivo para enviar: ");
                        String caminhoArquivo = scanner.nextLine();
                        File arquivo = new File(caminhoArquivo);
                        if (arquivo.exists() && arquivo.isFile()) {
                            output.writeObject(arquivo.getName());
                            output.writeObject(arquivo.length());
                            output.flush();
                            try (FileInputStream fileInput = new FileInputStream(arquivo)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = fileInput.read(buffer)) > 0) {
                                    output.write(buffer, 0, bytesRead);
                                }
                                output.flush();
                            }
                            System.out.println("Arquivo enviado com sucesso.");
                        } else {
                            System.out.println("Arquivo não encontrado ou caminho inválido.");
                        }
                    } else if (comando.equals("BAIXAR")) {
                        System.out.print("Digite o nome do arquivo para baixar: ");
                        String nomeArquivo = scanner.nextLine();
                        output.writeObject(nomeArquivo);
                        output.flush();

                        String respostaArquivo = (String) input.readObject();
                        if (respostaArquivo.equals("ARQUIVO_ENCONTRADO")) {
                            long tamanhoArquivo = (long) input.readObject();
                            System.out.println("Tamanho do arquivo: " + tamanhoArquivo + " bytes");
                            System.out.print("Digite o caminho completo para salvar o arquivo (incluindo o nome do arquivo): ");
                            String caminhoSalvar = scanner.nextLine();

                            File arquivoSalvar = new File(caminhoSalvar);

                            if (arquivoSalvar.isDirectory()) {
                                System.out.println("Erro: O caminho especificado é um diretório. Por favor, inclua o nome do arquivo.");
                                continue;
                            }

                            if (arquivoSalvar.exists()) {
                                System.out.println("Arquivo já existe no caminho: " + arquivoSalvar.getAbsolutePath());
                                System.out.println("Tamanho do arquivo existente: " + arquivoSalvar.length() + " bytes");

                                System.out.println("Escolha uma opção:");
                                System.out.println("1. Sobrescrever o arquivo");
                                System.out.println("2. Escolher um novo nome");
                                System.out.print("Opção: ");
                                String opcao = scanner.nextLine();

                                if (opcao.equals("1")) {
                                    try (FileOutputStream fileOutput = new FileOutputStream(arquivoSalvar)) {
                                        byte[] buffer = new byte[4096];
                                        int bytesRead;
                                        long totalBytesRead = 0;
                                        while (totalBytesRead < tamanhoArquivo) {
                                            bytesRead = input.read(buffer);
                                            fileOutput.write(buffer, 0, bytesRead);
                                            totalBytesRead += bytesRead;
                                        }
                                    }
                                    System.out.println("Arquivo sobrescrito com sucesso.");
                                } else if (opcao.equals("2")) {
                                    System.out.print("Digite o novo nome do arquivo: ");
                                    String novoNome = scanner.nextLine();
                                    File novoArquivo = new File(arquivoSalvar.getParent(), novoNome);
                                    try (FileOutputStream fileOutput = new FileOutputStream(novoArquivo)) {
                                        byte[] buffer = new byte[4096];
                                        int bytesRead;
                                        long totalBytesRead = 0;
                                        while (totalBytesRead < tamanhoArquivo) {
                                            bytesRead = input.read(buffer);
                                            fileOutput.write(buffer, 0, bytesRead);
                                            totalBytesRead += bytesRead;
                                        }
                                    }
                                    System.out.println("Arquivo salvo com o novo nome: " + novoNome);
                                } else {
                                    System.out.println("Opção inválida. Download cancelado.");
                                }
                            } else {
                                try (FileOutputStream fileOutput = new FileOutputStream(arquivoSalvar)) {
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
                            }
                        } else {
                            System.out.println("Arquivo não encontrado no servidor.");
                        }
                    } else if (comando.equals("LISTAR")) {
                        output.writeObject("LISTAR");
                        output.flush();

                        // Variável renomeada para evitar conflito de escopo
                        String respostaListar = (String) input.readObject();
                        if (respostaListar.equals("ESTRUTURA_DIRETORIOS")) {
                            String estrutura = (String) input.readObject();
                            System.out.println("Estrutura de diretórios e arquivos:");
                            System.out.println(estrutura);
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