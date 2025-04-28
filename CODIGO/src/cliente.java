import java.io.*;
import java.net.*;
import java.util.*;

public class cliente {
    private static final String SERVIDOR_IP = "localhost";
    private static final int PORTA = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVIDOR_IP, PORTA);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            if (!realizarLogin(output, input, scanner)) {
                return;
            }

            while (true) {
                System.out.println("\nEscolha uma opção: ENVIAR, BAIXAR, LISTAR, SAIR");
                String comando = scanner.nextLine().toUpperCase();
                output.writeObject(comando);
                output.flush();

                switch (comando) {
                    case "ENVIAR":
                        enviarArquivo(output, scanner);
                        break;
                    case "BAIXAR":
                        baixarArquivo(input, output, scanner);
                        break;
                    case "LISTAR":
                        listarArquivos(input);
                        break;
                    case "SAIR":
                        return;
                    default:
                        System.out.println("Comando inválido!");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro na conexão: " + e.getMessage());
        }
    }

    private static boolean realizarLogin(ObjectOutputStream output, ObjectInputStream input, Scanner scanner)
            throws IOException, ClassNotFoundException {
        System.out.print("Digite o nome de usuário: ");
        String usuario = scanner.nextLine();
        System.out.print("Digite a senha: ");
        String senha = scanner.nextLine();

        output.writeObject(usuario);
        output.writeObject(senha);
        output.flush();

        String resposta = (String) input.readObject();
        if (resposta.equals("LOGIN_SUCESSO")) {
            System.out.println("\nLogin bem-sucedido!");
            return true;
        } else {
            System.out.println("\nLogin falhou. Usuário ou senha incorretos.");
            return false;
        }
    }

    private static void enviarArquivo(ObjectOutputStream output, Scanner scanner) throws IOException {
        System.out.print("\nDigite o caminho do arquivo para enviar: ");
        String caminhoArquivo = scanner.nextLine();
        File arquivo = new File(caminhoArquivo);

        if (!arquivo.exists() || !arquivo.isFile()) {
            System.out.println("Arquivo não encontrado ou caminho inválido.");
            output.writeObject("ARQUIVO_INVALIDO");
            output.flush();
            return;
        }

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
    }

    private static void baixarArquivo(ObjectInputStream input, ObjectOutputStream output, Scanner scanner)
            throws IOException, ClassNotFoundException {
        System.out.print("\nDigite o nome do arquivo para baixar: ");
        String nomeArquivo = scanner.nextLine();
        output.writeObject(nomeArquivo);
        output.flush();

        String respostaArquivo = (String) input.readObject();
        if (!respostaArquivo.equals("ARQUIVO_ENCONTRADO")) {
            System.out.println("Arquivo não encontrado no servidor.");
            return;
        }

        long tamanhoArquivo = (long) input.readObject();
        System.out.println("Tamanho do arquivo: " + tamanhoArquivo + " bytes");

        while (true) {
            System.out.print("\nDigite o caminho para salvar (com nome do arquivo): ");
            String caminhoSalvar = scanner.nextLine();
            File arquivoDestino = new File(caminhoSalvar);


            if (arquivoDestino.isDirectory()) {
                System.out.println("Você especificou um diretório. Deseja:");
                System.out.println("1. Usar o nome original do arquivo");
                System.out.println("2. Digitar um novo caminho");
                System.out.print("Opção: ");
                String opcao = scanner.nextLine();

                if (opcao.equals("1")) {
                    arquivoDestino = new File(arquivoDestino, nomeArquivo);
                } else {
                    continue;
                }
            }

            if (arquivoDestino.exists()) {
                System.out.println("\nArquivo já existe no destino: " + arquivoDestino.getAbsolutePath());
                System.out.println("1. Sobrescrever");
                System.out.println("2. Transferir para outra pasta");
                System.out.println("3. Cancelar");
                System.out.print("Opção: ");
                String opcao = scanner.nextLine();

                if (opcao.equals("1")) {
                    downloadEConfirmacao(input, arquivoDestino, tamanhoArquivo);
                    break;
                } else if (opcao.equals("2")) {
                    System.out.print("\nDigite o novo caminho completo (com nome do arquivo): ");
                    String novoCaminho = scanner.nextLine();
                    File novoDestino = new File(novoCaminho);

                    if (novoDestino.exists()) {
                        System.out.println("Arquivo já existe no novo destino. Sobrescrever? (S/N)");
                        String sobrescrever = scanner.nextLine().toUpperCase();
                        if (sobrescrever.equals("S")) {
                            downloadEConfirmacao(input, novoDestino, tamanhoArquivo);
                            break;
                        }
                    } else {
                        downloadEConfirmacao(input, novoDestino, tamanhoArquivo);
                        break;
                    }
                } else {
                    output.writeObject("CANCELAR");
                    output.flush();
                    System.out.println("Download cancelado.");
                    break;
                }
            } else {
                downloadEConfirmacao(input, arquivoDestino, tamanhoArquivo);
                break;
            }
        }
    }

    private static void downloadEConfirmacao(ObjectInputStream input, File arquivoDestino, long tamanhoArquivo)
            throws IOException {
        try (FileOutputStream fileOutput = new FileOutputStream(arquivoDestino)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
            while (totalBytesRead < tamanhoArquivo) {
                bytesRead = input.read(buffer);
                fileOutput.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
            System.out.println("\nArquivo baixado com sucesso para: " + arquivoDestino.getAbsolutePath());
        }
    }

    private static void listarArquivos(ObjectInputStream input)
            throws IOException, ClassNotFoundException {
        String resposta = (String) input.readObject();
        if (resposta.equals("ESTRUTURA_DIRETORIOS")) {
            String estrutura = (String) input.readObject();
            System.out.println("\nEstrutura de diretórios e arquivos:");
            System.out.println(estrutura);
        } else {
            System.out.println("Erro ao listar arquivos.");
        }
    }
}