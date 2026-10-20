package com.credibanco.models;

public enum TipoTarjeta {
    CREDITO("123456"),
    DEBITO("654321");

    private final String idProducto;

    TipoTarjeta(String idProducto) {
        this.idProducto = idProducto;
    }

    public String getIdProducto() {
        return idProducto;
    }
}