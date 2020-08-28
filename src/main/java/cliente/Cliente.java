package cliente;

import servidor.Credenciais;
import servidor.SHA256;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Scanner;

public class Cliente{
    private String ip;
    private int porta;

    Socket clientSocket;
    ObjectOutputStream out;
    DatagramSocket socket;
    Scanner scanner;

    int idLeilao;
    int valorInicial;
    int valorLicitacoes;
    int nrPropostas;
    String user;
    String pass;

    public Cliente(String ip, int porta) {
        this.ip = ip;
        this.porta = porta;
        scanner = new Scanner(System.in);
    }
    public Cliente(String ip, int porta, int idLeilao, int valorInicial, int valorLicitacoes, int nrPropostas, String user, String pass) {
        this.ip = ip;
        this.porta = porta;
        this.idLeilao = idLeilao;
        this.valorInicial = valorInicial;
        this.valorLicitacoes = valorLicitacoes;
        this.nrPropostas = nrPropostas;
        this.user = user;
        this.pass = pass;
    }

    public void inicializar() throws IOException, NoSuchAlgorithmException {
        clientSocket = new Socket(ip, porta);
        out = new ObjectOutputStream((clientSocket.getOutputStream()));
        socket = new DatagramSocket(porta);
        fazerLogin();
        receberMensagens();
        mandarPedidos();
    }
    public void inicializarAutomatico() throws IOException {
        final Socket clientSocketA = new Socket(ip,porta);
        final ObjectOutputStream outA = new ObjectOutputStream(clientSocketA.getOutputStream());
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Credenciais c = new Credenciais(user, pass);
                    outA.writeObject(c);
                    int valor = valorInicial;
                    for(int i = 0; i < nrPropostas; i++) {
                        PedidoLicitacao p = new PedidoLicitacao('b', idLeilao, valor);
                        outA.writeObject(p);
                        valor+=valorLicitacoes;
                    }
                    outA.close();
                    clientSocketA.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
    private void receberMensagens() throws IOException {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    String mensagem;
                    while (true) {
                        byte[] buffer = new byte[256];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        mensagem = new String(packet.getData()).trim();
                        System.out.println(mensagem);
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }
    public void fazerLogin() throws NoSuchAlgorithmException, IOException {
        boolean passErrada = true;
        while(passErrada) {
            System.out.println("Introduza o username");
            String username = scanner.nextLine();
            System.out.println("Introduza a password");
            String password = scanner.nextLine();
            Credenciais credenciais = new Credenciais(username, password);
            out.writeObject(credenciais);
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String mensagem = new String(packet.getData()).trim();
            System.out.println(mensagem);
            if(!mensagem.equals("Credenciais Erradas, tente novamente")) {
                passErrada = false;
            }
        }

    }
    public void mandarPedidos() throws IOException {
        while (true) {
            String menuInicial = "Introduza uma das seguindes opções: \n a) criação de leilões \n b) criação de uma licitação \n c) consulta dos leilões disponíveis \n";
            System.out.println(menuInicial);
            String mensagem =  scanner.nextLine();
            char keyWord = mensagem.charAt(0);
            if(mensagem.length() > 1) {
                keyWord = 'z';
            }
            switch (keyWord) {
                case 'a' :
                    criarPedidoLeilao();
                    break;
                case 'b':
                    criarPedidoLicitacao();
                    break;
                case 'c':
                    consultarLeiloes();
                    break;
                default:
                    System.out.println("Tente uma das letras do menu \n");
                    break;
            }
        }
    }
    public void criarPedidoLeilao() throws IOException {
        System.out.println("Introduza o ano da data que expira");
        Integer ano = Integer.parseInt(scanner.nextLine());
        System.out.println("Introduza o mes da data que expira");
        Integer mes = Integer.parseInt(scanner.nextLine()) ;
        System.out.println("Introduza o dia da data que expira");
        Integer dia = Integer.parseInt(scanner.nextLine()) ;
        System.out.println("Introduza a hora da data que expira");
        Integer hora = Integer.parseInt(scanner.nextLine()) ;
        System.out.println("Introduza os minutos da data que expira");
        Integer min = Integer.parseInt(scanner.nextLine()) ;
        System.out.println("introduza a descrição do objeto");
        String descricao = scanner.nextLine();
        System.out.println("Introduza o valor inicial do produto");
        Integer valor = Integer.parseInt(scanner.nextLine()) ;
        PedidoLeilao p = new PedidoLeilao('a', ano, mes, dia, hora, min, descricao, valor);
        out.writeObject(p);
    }
    public void criarPedidoLicitacao() throws IOException {
        System.out.println("Introduza o leilão que pretende");
        Integer idPretendido = Integer.parseInt(scanner.nextLine());
        System.out.println("Introduza o valor da licitação");
        Integer valorLicitacao = Integer.parseInt(scanner.nextLine());
        PedidoLicitacao p = new PedidoLicitacao('b',idPretendido,valorLicitacao);
        out.writeObject(p);
    }
    public void consultarLeiloes() throws IOException {
        Pedido p = new Pedido('c');
        out.writeObject(p);
    }
}
