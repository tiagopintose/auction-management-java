package cliente;

import java.io.Serializable;

public class Pedido implements Serializable  {
    char keyWord;

    public Pedido() { }

    public Pedido(char keyWord) {
        this.keyWord = keyWord;
    }

    public char getKeyWord() {
        return keyWord;
    }
}
