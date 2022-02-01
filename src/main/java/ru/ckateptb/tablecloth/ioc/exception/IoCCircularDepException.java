package ru.ckateptb.tablecloth.ioc.exception;

public class IoCCircularDepException extends Exception {
    public IoCCircularDepException(String message) {
        super(message);
    }
}
