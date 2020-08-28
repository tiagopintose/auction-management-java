package cliente;

public class PedidoLicitacao extends Pedido {
    int idLicitacao;
    int valorLicitacao;
    public PedidoLicitacao(char keyWord, int idLicitacao, int valorLicitacao) {
        this.keyWord = keyWord;
        this.idLicitacao = idLicitacao;
        this.valorLicitacao = valorLicitacao;
    }

    public int getIdLicitacao() {
        return idLicitacao;
    }

    public int getValorLicitacao() {
        return valorLicitacao;
    }
}
