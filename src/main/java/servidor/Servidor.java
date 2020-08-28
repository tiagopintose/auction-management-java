package servidor;

import cliente.Cliente;
import cliente.Pedido;
import cliente.PedidoLeilao;
import cliente.PedidoLicitacao;
import leilao.Leilao;
import licitador.Licitador;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.rmi.activation.ActivationGroup_Stub;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class Servidor {
    static public ServerSocket serverSocket;

    List<Leilao> leiloes;
    List<Credenciais> registos;
    List<Licitador> clientes;


    List<InetAddress> addresses;

    int port;


    public Servidor(int porta) throws IOException, ClassNotFoundException {
        this.port = porta;
        serverSocket = new ServerSocket(porta);
        addresses = Collections.synchronizedList(new ArrayList<InetAddress>());
    }

    public void start() throws IOException {
        System.out.println("O servidor foi inicializado!");
        leiloes = (List)Serializador.deserialize("Leiloes");
        verificaSeAcabou();
        if(leiloes == null) {
            leiloes = Collections.synchronizedList(new ArrayList<Leilao>());
            Serializador.serialize(leiloes, "Leiloes");
        }
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("New Connection");
                        InetAddress address = clientSocket.getInetAddress();
                        System.out.println(address.getHostAddress());
                        addresses.add(address);
                        processarPedidos(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

    }
    private void processarPedidos(final Socket clientSocket) throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                    Licitador licitadorAtual = login(clientSocket, in);
                    while (true) {
                        System.out.println("olaaa");
                        Pedido p = (Pedido)in.readObject();
                        switch (p.getKeyWord()) {
                            case 'a':
                                criarLeilao(clientSocket, licitadorAtual, p);
                                break;
                            case 'b':
                                criarUmaLicitacao(clientSocket, licitadorAtual,p);
                                break;
                            case 'c':
                                consultarLeiloes(clientSocket, licitadorAtual);
                                break;
                            default:
                                break;
                        }
                    }


                }
                catch (SocketException e) {
                    System.err.println("LIGACAO CAIU");
                    addresses.remove(clientSocket.getInetAddress().getHostAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        });thread.start();
    }
    private Licitador login(Socket clientSocket, ObjectInputStream in) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        boolean passErrada = true;
        Licitador licitadorAtual = null;
        while (passErrada) {
            Credenciais c1 = (Credenciais) in.readObject();
            registos = (List) Serializador.deserialize("Registos");
            clientes = (List)Serializador.deserialize("Clientes");
            if(registos == null) {
                registos = Collections.synchronizedList(new ArrayList<Credenciais>());
                Serializador.serialize(registos, "Registos");
            }
            if(clientes == null) {
                clientes = Collections.synchronizedList(new ArrayList<Licitador>());
                Serializador.serialize(clientes, "Clientes");
            }
            String passCifrada = SHA256.generate(c1.getPassword().getBytes());
            c1.setPassword(passCifrada);
            for(Credenciais credenciais: registos) {
                if(credenciais.getUsername().equals(c1.getUsername()) && credenciais.getPassword().equals(c1.getPassword())) {
                    Licitador licitador = getLicitadorByCredencials(credenciais, clientes);
                    if(licitador == null) {
                        licitadorAtual = new Licitador(c1.getUsername(), c1.getPassword(), credenciais.getPlafondInicial(), clientSocket.getInetAddress());
                        clientes.add(licitadorAtual);
                        Serializador.serialize(clientes, "Clientes");
                        System.out.println("primeiro login do: " + c1.getUsername());
                    } else {
                        //caso o user faça log in noutro pc
                        licitador.setAddress(clientSocket.getInetAddress());
                        Serializador.serialize(clientes, "Clientes");
                        licitadorAtual = licitador;
                    }
                    passErrada = false;
                }
            }
            if(passErrada) {
                envia(clientSocket.getInetAddress(),"Credenciais Erradas, tente novamente");
            } else {
                envia(clientSocket.getInetAddress(), "Login feito com sucesso!");
            }
        }
        return licitadorAtual;
    }
    private void verificaSeAcabou() {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        if(leiloes != null) {
                            for(Leilao leilao : leiloes) {
                                if(!leilao.getExpirou()) {
                                    if(leilao.getDataLimite().before(new Date(System.currentTimeMillis()))) {
                                        System.out.println("entrou");
                                        leilao.setExpirou(true);
                                        Licitador autor = getLicitadorByNameAndPassord(leilao.getAutor(), clientes);
                                        if(leilao.getVencedorAtual() == null) {
                                            mostrarAUmLicitador(autor, "Lamentamos, mas o seu leilão com o ID " + leilao.getId() + " fechou sem qualquer licitação");
                                        } else {
                                            Licitador vencedorAtual = getLicitadorByNameAndPassord(leilao.getVencedorAtual(), clientes);
                                            mostrarAUmLicitador(vencedorAtual,  "Parabéns! Foi o vencedor do leilão com o ID " + leilao.getId() + " no valor de " + leilao.getPropostaMaisAlta().getValor() + " euros.");
                                            mostrarAUmLicitador(autor, "O bem presente no leilão com o ID " + leilao.getId() + " foi vendido à pessoa " + vencedorAtual.getUsername() + " com o valor de " + leilao.getPropostaMaisAlta().getValor() + " euros.");
                                            for(Licitador l: leilao.getLicitadores()) {
                                                Licitador licitador = getLicitadorByNameAndPassord(l, clientes);
                                                if(!licitador.equals(vencedorAtual)) {
                                                    mostrarAUmLicitador(licitador, "O Leilão com o ID " + leilao.getId() + " no qual realizou licitações já fechou, infelizmente voçê não foi o vencedor");
                                                }
                                            }
                                            autor.reporPLafond(leilao.getPropostaMaisAlta().getValor());
                                        }
                                        Serializador.serialize(leiloes, "Leiloes");
                                        Serializador.serialize(clientes, "Clientes");
                                    }
                                }

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    private void criarLeilao(Socket clientSocket, Licitador licitadorAtual, Pedido pedido) throws IOException, ClassNotFoundException {
        PedidoLeilao p = (PedidoLeilao) pedido;
        System.out.println(clientSocket.getInetAddress().getHostAddress());
        System.out.println(licitadorAtual);
        int leilaoIDCount = leiloes.size()+1;
        Leilao leilao = new Leilao(leilaoIDCount, p.getAno(), p.getMes(), p.getDia(), p.getHora(), p.getMin(),licitadorAtual, p.getDescricao(), p.getValor());
        mostraParaTodosMenosOQuePediu(clientSocket, "Há um novo leilão disponível, queira consultar os leilões disponíveis");
        mostrarApenasAoQuePediu(clientSocket, "O seu leilão foi criado com sucesso com o ID " + leilao.getId());
        leiloes.add(leilao);
        Serializador.serialize(leiloes, "Leiloes");
    }
    private void criarUmaLicitacao(Socket clientSocket, Licitador licitadorAtual, Pedido pedido) throws IOException, ClassNotFoundException {
        PedidoLicitacao p = (PedidoLicitacao) pedido;
        int idPretendido = p.getIdLicitacao();
        if(existeLeilao(idPretendido)) {
            int valorLicitacao = p.getValorLicitacao();
            Leilao leilaoPretendido =getLeilaoById(idPretendido, leiloes);
            if(valorLicitacao > leilaoPretendido.getPropostaMaisAlta().getValor()) {
                if(valorLicitacao <= licitadorAtual.getPlafond() || eMesmoLicitador(leilaoPretendido.getPropostaMaisAlta(), licitadorAtual, valorLicitacao)) {
                    mostraParaTodosMenosOQuePediu(clientSocket, "Foi recebida uma nova licitação no leilão com ID " + idPretendido);
                    mostrarApenasAoQuePediu(clientSocket, "A sua licitação foi aceite.");
                    leilaoPretendido.setVencedorAtual(licitadorAtual);
                    leilaoPretendido.adicionarLicitador(licitadorAtual);
                    if(leilaoPretendido.getPropostaMaisAlta().getLicitador() != null) {
                        Licitador licitadorPretendido = getLicitadorByNameAndPassord(leilaoPretendido.getPropostaMaisAlta().getLicitador(), clientes);
                        System.out.println("entrou aquiii");
                        System.out.println(leilaoPretendido.getPropostaMaisAlta().getLicitador());
                        licitadorPretendido.reporPLafond(leilaoPretendido.getPropostaMaisAlta().getValor());
                        System.out.println(leilaoPretendido.getPropostaMaisAlta().getLicitador());
                    }
                    licitadorAtual.retirarPlafond(valorLicitacao);
                    Proposta proposta = new Proposta(licitadorAtual, valorLicitacao);
                    leilaoPretendido.setPropostaMaisAlta(proposta);
                    Serializador.serialize(leiloes,"Leiloes");
                    Serializador.serialize(clientes, "Clientes");
                } else {
                    mostrarApenasAoQuePediu(clientSocket, "A sua solicitação não foi aceite, o valor da sua proposta é superior ao seu plafond.");
                }
            } else {
                mostrarApenasAoQuePediu(clientSocket, "A sua proposta não foi aceite, o valor proposto não é superior ao máximo atual");
            }
        } else {
            mostrarApenasAoQuePediu(clientSocket, "O leilão com id " + idPretendido + " não existe ou já não está disponível");
        }
    }
    private void consultarLeiloes(Socket clientSocket, Licitador licitadorAtual) throws IOException, ClassNotFoundException {
        mostrarApenasAoQuePediu(clientSocket, "Plafond disponivel: " + licitadorAtual.getPlafond());
        for(Leilao leilao : leiloes) {
            if(!leilao.getExpirou()) {
                mostrarApenasAoQuePediu(clientSocket, leilao.toString());
            }

        }
    }
    private boolean eMesmoLicitador(Proposta p, Licitador l2, int valorProposta) {
        if(p.getLicitador().getUsername().equals(l2.getUsername()) && p.getLicitador().getPassword().equals(l2.getPassword())) {
            int plafondDisponivel = p.getValor() + l2.getPlafond();
            if(plafondDisponivel - valorProposta >= 0) {
                return true;
            } else {
                return false;
            }

        }
        return false;
    }
    private void mostrarApenasAoQuePediu(Socket clientSocket, String mensagem) throws IOException{
        for(InetAddress inetAddress: addresses) {
            if(inetAddress.equals(clientSocket.getInetAddress())) {
                envia(inetAddress, mensagem);
            }
        }
    }
    private void mostraParaTodosMenosOQuePediu(Socket clientSocket, String mensagem) throws IOException {
        for(InetAddress inetAddress: addresses) {
            if(!inetAddress.equals(clientSocket.getInetAddress())) {
                envia(inetAddress, mensagem);
            }
        }
    }
    private void mostrarAUmLicitador(Licitador licitador, String mensagem) throws IOException{
        envia(licitador.getAddress(), mensagem);
    }
    private void envia(InetAddress address, String mensagem) throws IOException{
        DatagramPacket packet = new DatagramPacket(mensagem.getBytes(), mensagem.getBytes().length, address, port);
        DatagramSocket out = new DatagramSocket();
        out.send(packet);
    }
    private Licitador getLicitadorByCredencials(Credenciais credenciais, List<Licitador> clientes) throws IOException, ClassNotFoundException {
        for(Licitador l: clientes) {
            if(credenciais.getUsername().equals(l.getUsername()) && credenciais.getPassword().equals(l.getPassword())) {
                return l;
            }
        }
        return null;
    }

    private boolean existeLeilao(int idPretendido) {
        for(Leilao leilao: leiloes) {
            if(leilao.getId() == idPretendido && !leilao.getExpirou()) {
                return true;
            }
        }
        return false;
    }
    private Licitador getLicitadorByNameAndPassord(Licitador licitador, List<Licitador> clientes) {
        for(Licitador l : clientes) {
            if(l.getUsername().equals(licitador.getUsername()) && l.getPassword().equals(licitador.getPassword())) {
                return l;
            }
        }
        return null;
    }
    private Leilao getLeilaoById(int id, List<Leilao> leiloes) {
        for(Leilao leilao: leiloes) {
            if(leilao.getId() == id) {
                return leilao;
            }
        }
        return null;
    }
}
