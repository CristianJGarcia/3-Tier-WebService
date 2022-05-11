package model;

import java.util.ArrayList;

public class FetchResults {
    private int numRows;
    private int pageSize;
    private int currentPage;
    private ArrayList<Person> people;

    public FetchResults() {
        this.pageSize = 10;
        this.people = new ArrayList<Person>();
    }

    public int getNumRows() {
        return numRows;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public ArrayList<Person> getPeople() {
        return people;
    }

    public void setPeople(ArrayList<Person> people) {
        this.people = people;
    }

    public void addPerson(Person person){
        this.people.add(person);
    }
}
