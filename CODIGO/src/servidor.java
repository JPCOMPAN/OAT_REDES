import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class servidor {
    private static final int PORTA = 12345;
    private static final String CAMINHO_BASE = "C:\\Users\\pablo\\Documents\\OAT_REDES\\OAT_REDES_DIRETORIO";
    private static final String[] USUARIOS = {"usuario1", "usuario2", "usuario3"};
    private static final String[] SENHAS = {"senha1", "senha2", "senha3"};
    private static final String[] SUBPASTAS = {"pdf", "jpg", "txt"};

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor iniciado na porta " + PORTA);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClienteHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClienteHandler implements Runnable {
        private Socket socket;

        public ClienteHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {

                // Autenticação do usuário
                String usuario = (String) input.readObject();
                String senha = (String) input.readObject();

                boolean loginConcluido = false;
                for (int i = 0; i < USUARIOS.length; i++) {
                    if (usuario.equals(USUARIOS[i]) && senha.equals(SENHAS[i])) {
                        loginConcluido = true;
                        break;
                    }
                }

                if (loginConcluido) {
                    output.writeObject("LOGIN_SUCESSO");
                    criarPastasUsuario(usuario);
                    gerenciarArquivos(usuario, input, output);
                } else {
                    output.writeObject("LOGIN_FALHA");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void criarPastasUsuario(String usuario) throws IOException {
            Path caminhoPasta = Paths.get(CAMINHO_BASE, usuario);
            if (!Files.exists(caminhoPasta)) {
                Files.createDirectories(caminhoPasta);
                for (String subpasta : SUBPASTAS) {
                    Path pathSubpasta = Paths.get(caminhoPasta.toString(), subpasta);
                    Files.createDirectories(pathSubpasta);
                }
                System.out.println("Pastas criadas para o usuário: " + usuario);
            }
        }

        private void gerenciarArquivos(String usuario, ObjectInputStream input, ObjectOutputStream output) throws IOException, ClassNotFoundException {
            while (true) {
                String comando = (String) input.readObject();
                if (comando.equals("ENVIAR")) {

                    String nomeArquivo = (String) input.readObject();
                    long tamanhoArquivo = (long) input.readObject();

                    String extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf(".") + 1).toLowerCase();


                    String subpasta = null;
                    for (String sp : SUBPASTAS) {
                        if (sp.equalsIgnoreCase(extensao)) {
                            subpasta = sp;
                            break;
                        }
                    }

                    Path caminhoArquivo;
                    if (subpasta != null) {
                        caminhoArquivo = Paths.get(CAMINHO_BASE, usuario, subpasta, nomeArquivo);
                    } else {
                        caminhoArquivo = Paths.get(CAMINHO_BASE, usuario, nomeArquivo);
                    }

                    try (FileOutputStream fileOutput = new FileOutputStream(caminhoArquivo.toFile())) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long totalBytesRead = 0;
                        while (totalBytesRead < tamanhoArquivo) {
                            bytesRead = input.read(buffer);
                            fileOutput.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                        }
                    }
                    output.writeObject("ARQUIVO_RECEBIDO");
                    System.out.println("Arquivo salvo: " + caminhoArquivo.toString());
                } else if (comando.equals("BAIXAR")) {

                    String nomeArquivo = (String) input.readObject();
                    Path caminhoArquivo = null;
                    boolean arquivoEncontrado = false;

                    for (String user : USUARIOS) {
                        for (String subpasta : SUBPASTAS) {
                            caminhoArquivo = Paths.get(CAMINHO_BASE, user, subpasta, nomeArquivo);
                            System.out.println("Procurando arquivo em: " + caminhoArquivo.toString());
                            if (Files.exists(caminhoArquivo)) {
                                arquivoEncontrado = true;
                                System.out.println("Arquivo encontrado em: " + caminhoArquivo.toString());
                                break;
                            }
                        }
                        if (arquivoEncontrado) break;
                    }

                    if (arquivoEncontrado) {
                        output.writeObject("ARQUIVO_ENCONTRADO");
                        output.writeObject(Files.size(caminhoArquivo));
                        try (FileInputStream fileInput = new FileInputStream(caminhoArquivo.toFile())) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = fileInput.read(buffer)) > 0) {
                                output.write(buffer, 0, bytesRead);
                            }
                            output.flush();
                        }
                        System.out.println("Arquivo enviado para o cliente: " + nomeArquivo);
                    } else {
                        output.writeObject("ARQUIVO_NAO_ENCONTRADO");
                        System.out.println("Arquivo não encontrado: " + nomeArquivo);
                    }
                } else if (comando.equals("SAIR")) {
                    break;
                }
            }
        }
    }
}