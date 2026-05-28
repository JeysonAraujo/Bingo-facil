package com.bingoai.backend.ticket;

import java.util.List;

public class CardMatrix {
    private List<Integer> b;
    private List<Integer> i;
    private List<Integer> n;
    private List<Integer> g;
    private List<Integer> o;

    public CardMatrix() {}

    public CardMatrix(List<Integer> b, List<Integer> i, List<Integer> n, List<Integer> g, List<Integer> o) {
        this.b = b;
        this.i = i;
        this.n = n;
        this.g = g;
        this.o = o;
    }

    public List<Integer> getB() { return b; }
    public void setB(List<Integer> b) { this.b = b; }

    public List<Integer> getI() { return i; }
    public void setI(List<Integer> i) { this.i = i; }

    public List<Integer> getN() { return n; }
    public void setN(List<Integer> n) { this.n = n; }

    public List<Integer> getG() { return g; }
    public void setG(List<Integer> g) { this.g = g; }

    public List<Integer> getO() { return o; }
    public void setO(List<Integer> o) { this.o = o; }
}
