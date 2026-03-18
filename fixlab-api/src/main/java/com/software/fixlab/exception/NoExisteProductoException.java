package com.software.fixlab.exception;

public class NoExisteProductoException extends RuntimeException {
    public NoExisteProductoException(String message) {
        super(message);
    }
}