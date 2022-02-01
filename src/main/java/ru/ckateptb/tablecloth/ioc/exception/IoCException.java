package ru.ckateptb.tablecloth.ioc.exception;

public class IoCException extends RuntimeException {
    public IoCException(Throwable cause) {
        super(cause);
    }

    public IoCException(String message) {
        super(message);
    }
}