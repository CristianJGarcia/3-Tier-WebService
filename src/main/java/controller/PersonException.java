package controller;

public class PersonException extends RuntimeException {
    public PersonException(Exception e) { super(e); }

    public PersonException(String msg) { super(msg); }
}
