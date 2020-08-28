package cliente;

public class PedidoLeilao extends Pedido{
    int ano;
    int mes;
    int dia;
    int hora;
    int min;
    String descricao;
    int valor;
    public PedidoLeilao(char keyWord, int ano,int mes,int dia,int hora,int min,String descricao,int valor) {
        this.keyWord = keyWord;
        this.ano = ano;
        this.dia = dia;
        this.mes = mes;
        this.hora = hora;
        this.min = min;
        this.descricao = descricao;
        this.valor = valor;
    }

    public int getAno() {
        return ano;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getDia() {
        return dia;
    }

    public int getHora() {
        return hora;
    }

    public int getMes() {
        return mes;
    }

    public int getMin() {
        return min;
    }

    public int getValor() {
        return valor;
    }
}
