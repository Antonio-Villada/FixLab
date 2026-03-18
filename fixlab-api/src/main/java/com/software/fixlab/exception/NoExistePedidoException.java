package com.software.fixlab.exception;

public class NoExistePedidoException extends RuntimeException {
    public NoExistePedidoException(String message) {
        super(message);
    }
}