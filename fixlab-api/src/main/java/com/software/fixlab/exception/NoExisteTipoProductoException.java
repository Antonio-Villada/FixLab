package com.software.fixlab.exception;

public class NoExisteTipoProductoException extends RuntimeException {
    public NoExisteTipoProductoException(String message) {
        super(message);
    }
}