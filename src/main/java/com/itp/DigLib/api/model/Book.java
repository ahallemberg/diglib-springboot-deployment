package com.itp.DigLib.api.model;

import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a book with a title, author, year, genre, ISBN, and file path.
 */
public final class Book {
    private static int lastId = 0; // Static variable to keep track of the last assigned ID
    private int id;
    private String title;
    private String author;
    private int year;
    private String genre;
    private String isbn;
    private String filename;

    /**
     * Constructs a new Book object with the specified details.
     *
     * @param title the title of the book
     * @param author the author of the book
     * @param genre the genre of the book
     * @param isbn the ISBN of the book, which must be 13 digits
     * @param year the year of publication, which must be between 0 and 2025
     * @throws IllegalArgumentException if any of the fields are null, empty, or invalid
     */

    public Book(String title, String author, String genre, String isbn, int year) throws IllegalArgumentException  {
        if(year < 0 || year > 2025) {
            throw new IllegalArgumentException("Year must be between 0 and 2025");
        } 
        if(title.isEmpty() || author.isEmpty() || genre.isEmpty() || isbn.isEmpty()) {
            throw new IllegalArgumentException("No fields can be empty");
        }
        if(!isbn.matches("[0-9]{13}")) {
            throw new IllegalArgumentException("ISBN must be 13 digits");
        }
        
        this.filename = toCamelCase(title) + ".txt";
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
        this.isbn = isbn;
        this.id = ++lastId; // Increment the lastId and assign it to the id
    }

    /**
     * Returns the title of the book.
     *
     * @return the title of the book
     */
    public String getTitle() {
        return title;
    }

        /**
     * Returns the title of the book.
     *
     * @return the title of the book
     */
    public int getID() {
        return id;
    }


    /**
     * Returns the file name of the book.
     *
     * @return the file name of the book
     */
    public String getFileName() {
        return filename;
    }

    /**
     * Returns the author of the book.
     *
     * @return the author of the book
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the year of publication of the book.
     *
     * @return the year of publication of the book
     */
    public int getYear() {
        return year;
    }

    /**
     * Returns the genre of the book.
     *
     * @return the genre of the book
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Returns the ISBN of the book.
     *
     * @return the ISBN of the book
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Returns the file path of the book.
     *
     * @param getBookDir the directory where the book is stored
     * 
     * @return the file path of the book
     */
    @JsonIgnore
    public String getFilePath(String getBookDir) {
        return Paths.get(getBookDir, filename).toString();
    }
    /**
     * Returns the metadata of the book as a formatted string.
     * The metadata includes the title, author, genre, ISBN, and year of the book.
     * 
     * <p>The format of the returned string is:</p>
     * <pre>
     * Title: [title]
     * Author: [author]
     * Genre: [genre]
     * ISBN: [isbn]
     * Year: [year]
     * </pre>
     * 
     * @return a string containing the metadata of the book.
     */
    
    @JsonIgnore
    public String getMetadata() {
        return "Title: " + title + "\nAuthor: " + author + "\nGenre: " + genre + "\nISBN: " + isbn + "\nYear: " + year;
    }

    /**
     * Converts a given string to camel case format.
     * For example, "the great gatsby has returned" becomes "TheGreatGatsbyHasReturned".
     *
     * @param input the input string to be converted
     * @return the camel case formatted string
     */
    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.split(" ");
        StringBuilder camelCaseString = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                camelCaseString.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1).toLowerCase());
            }
        }
    
        return camelCaseString.toString(); 
    }


}