package model;

import controller.PersonException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Person {
    private static final Logger logger = LogManager.getLogger();

    public static final int NEW_PERSON = 0;

    private int id;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private LocalDateTime last_modified;
    private int age = 0;

    public Person(){

    }

    public Person(int i, String f, String l, LocalDate d, int a, LocalDateTime last){
        id = i;
        firstName = f;
        lastName = l;
        dob = d;
        age = a;
        last_modified = last;
    }

    public static boolean isValidPersonName(String n) {
        if(n == null)
            return false;

        if(n.length() < 1 || n.length() > 100)
            return false;

        return true;
    }

    public void setPersonName(String fName, String lName) throws PersonException {
        if(!isValidPersonName(fName))
            throw new PersonException("Person first name must be between 1 and 100 characters");
        if(!isValidPersonName(lName))
            throw new PersonException("Person last name must be between 1 and 100 characters");
        this.firstName = fName;
        this.lastName = lName;
    }

    @Override
    public String toString() {
        return  firstName + " " + lastName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public LocalDateTime getLastModified() {
        return last_modified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.last_modified = lastModified;
    }
}
