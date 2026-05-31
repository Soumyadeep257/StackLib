package com.pinnacle;

public class Book {
    private int id;
    private String name;
    private String author;
    private String category;
    private int quantity;

    public Book(int id, String name, String author, String category, int quantity) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.category = category;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
}