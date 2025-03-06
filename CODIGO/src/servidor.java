import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class servidor {
    public static void main(String[]args){
        String[] usuarios = {"usuario1", "usuario2", "usuario3"};
        String[] senhas = {"senha1", "senha2", "senha3"};

        Scanner scanner = new Scanner(System.in);

        System.out.println("Digite o nome de usuario: ");
        String usuarioDigitado = scanner.nextLine();

        System.out.println("Digite a senha: ");
        String senhaDigitada = scanner.nextLine();

        boolean loginConcluido = false;

        for (int i = 0; i < usuarios.length; i++){
            if (usuarioDigitado.equals(usuarios[i])){
                if (senhaDigitada.equals(senhas[i])){
                    loginConcluido = true;
                    break;
                } else {
                    System.out.println("Senha incorreta para o " + usuarios[i]);
                }
            }
        }
        String caminhoBase = "D:\\Faculdade\\Nova pasta\\OAT_REDES_DIRETORIO";
        String caminhoPasta = caminhoBase + "\\" + usuarioDigitado;
        Path path = Paths.get(caminhoPasta);
        String[] subPastas = {"pdf", "jpg", "txt"};
        if (loginConcluido){
            if (Files.exists(path)){
                System.out.println("A pasta para o " + usuarioDigitado + "já existe!");
            } else {
                try {
                    Files.createDirectories(path);
                    System.out.println("Pasta criada com sucesso: " + caminhoPasta);
                    for (String subpastas: subPastas){
                        Path pathSubpasta = Paths.get(caminhoPasta + "\\" + subpastas);
                        Files.createDirectories(pathSubpasta);
                        System.out.println("Subpasta criada: " + pathSubpasta);
                    }
                } catch (IOException e){
                    System.out.println("Falha ao criar a pasta : " + e.getMessage());
                }
            }
        } else {
            System.out.print("Usuario não encontrado ou senha incorreta");
        }
    }
}
