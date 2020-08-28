package leilao;

import licitador.Licitador;
import servidor.Proposta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Leilao implements Serializable {
    int id;
    Licitador autor;
    String descricao;
    Date dataLimite;
    boolean expirou = false;
    int valor;
    Licitador vencedorAtual;
    Proposta propostaMaisAlta;
    ArrayList<Licitador> licitadores = new ArrayList<Licitador>();

    public Leilao(int id, int ano, int mes, int dia, int hora, int min, Licitador autor, String descricao, int valor) {
        this.id = id;
        this.dataLimite = new Date(ano - 1900, mes - 1, dia, hora, min);
        if(dataLimite.before(new Date(System.currentTimeMillis()))) {
            this.expirou = true;
        }
        this.autor = autor;
        this.descricao = descricao;
        this.valor = valor;
        propostaMaisAlta = new Proposta(null, valor);
    }

    public int getId() {
        return id;
    }
    public Date getDataLimite() {
        return dataLimite;
    }
    public Licitador getAutor() {
        return autor;
    }
    public boolean getExpirou() {
        return expirou;
    }

    public ArrayList<Licitador> getLicitadores() {
        return licitadores;
    }
    public Licitador getVencedorAtual() {
        return vencedorAtual;
    }
    public Proposta getPropostaMaisAlta() {
        return propostaMaisAlta;
    }
    public int getValor() {
        return valor;
    }
    public void setPropostaMaisAlta(Proposta propostaMaisAlta) {
        this.propostaMaisAlta = propostaMaisAlta;
    }
    public void setVencedorAtual(Licitador vencedorAtual) {
        this.vencedorAtual = vencedorAtual;
    }
    public void setExpirou(boolean expirou) {
        this.expirou = expirou;
    }

    public String toString() {
        if(propostaMaisAlta != null) {
            return id + " " +  descricao + " " + dataLimite + " " + propostaMaisAlta.getValor() + " " + autor.getUsername() ;
        } else {
            return id + " " +  descricao + " " + dataLimite + " " + valor + " " + autor.getUsername() ;
        }

    }
    public void adicionarLicitador(Licitador licitadorAtual) {
        boolean existe = false;
        for(Licitador l : licitadores) {
            if(l.getUsername().equals(licitadorAtual.getUsername()) && l.getPassword().equals(licitadorAtual.getPassword())) {
                existe = true;
            }
        }
        if(!existe) {
            licitadores.add(licitadorAtual);
        }
    }

}
