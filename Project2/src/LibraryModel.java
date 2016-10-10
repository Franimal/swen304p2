/*
 * LibraryModel.java
 * Author:
 * Created on:
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class LibraryModel {

	// For use in creating dialogs and making them modal
	private JFrame dialogParent;

	private String url;
	private Connection conn;
	private PreparedStatement stmt;
	private ResultSet res;

	public LibraryModel(JFrame parent, String userid, String password) {
		dialogParent = parent;
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		this.url = "jdbc:postgresql://db.ecs.vuw.ac.nz/" + userid + "_jdbc";
		try {
			this.conn = DriverManager.getConnection(url, userid, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Connected to database " + url);
	}

	public String bookLookup(int isbn) {

		try {

			String result = "Book Lookup\r\n";
			Book book = new Book();

			String query = "SELECT * FROM Book WHERE ISBN = ?;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, isbn);
			res = stmt.executeQuery();

			if (!res.isBeforeFirst()) {
				return result + "No Results.\r\n";
			}

			res.next();
			book.ISBN = res.getInt("ISBN");
			book.Title = res.getString("Title");
			book.NumOfCop = res.getInt("NumOfCop");
			book.NumLeft = res.getInt("NumLeft");

			query = "SELECT AuthorSeqNo, Name, Surname FROM Author NATURAL JOIN (SELECT * FROM Book_Author WHERE ISBN = ? ORDER BY AuthorSeqNo) AS BookAuthor;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, book.ISBN);
			res = stmt.executeQuery();

			while (res.next()) {
				Author auth = new Author();
				auth.Name = res.getString("Name").trim();
				auth.Surname = res.getString("Surname").trim();
				book.authors.add(auth);
			}

			result += book.toFullString();

			return result;

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(dialogParent, e.getMessage(),
					"Database Error", JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
			return "";
		} finally {
			try {
				res.close();
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public String showCatalogue() {
		try {

			String result = "Catalogue\r\n\r\n";

			String query = "SELECT * FROM Book ORDER BY ISBN;";
			stmt = conn.prepareStatement(query);
			res = stmt.executeQuery();

			if (!res.isBeforeFirst()) {
				return result + "No Results.\r\n";
			}
			List<Book> books = new ArrayList<>();
			
			while (res.next()) {
				Book book = new Book();
				book.ISBN = res.getInt("ISBN");
				book.Title = res.getString("Title");
				book.NumOfCop = res.getInt("NumOfCop");
				book.NumLeft = res.getInt("NumLeft");
				books.add(book);				
			}
			
			stmt.close();
			res.close();
			
			for(Book book : books){
				query = "SELECT AuthorSeqNo, Name, Surname FROM Author NATURAL JOIN (SELECT * FROM Book_Author WHERE ISBN = ? ORDER BY AuthorSeqNo) AS BookAuthor;";
				stmt = conn.prepareStatement(query);
				stmt.setInt(1, book.ISBN);
				res = stmt.executeQuery();

				while (res.next()) {
					Author auth = new Author();
					auth.Name = res.getString("Name").trim();
					auth.Surname = res.getString("Surname").trim();
					book.authors.add(auth);
				}

				result += book.toFullString();
			}

			return result;

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(dialogParent, e.getMessage(),
					"Database Error", JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
			return "";
		} finally {
			try {
				res.close();
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public String showLoanedBooks() {
		try {

			String result = "Loaned Books\r\n\r\n";

			String query = "SELECT * FROM Book WHERE ISBN IN (SELECT ISBN FROM Cust_book);";
			stmt = conn.prepareStatement(query);
			res = stmt.executeQuery();

			if (!res.isBeforeFirst()) {
				return result + "No Results.\r\n";
			}
			List<Book> books = new ArrayList<>();
			
			while (res.next()) {
				Book book = new Book();
				book.ISBN = res.getInt("ISBN");
				book.Title = res.getString("Title");
				book.NumOfCop = res.getInt("NumOfCop");
				book.NumLeft = res.getInt("NumLeft");
				books.add(book);				
			}
			
			stmt.close();
			res.close();
			
			for(Book book : books){
				query = "SELECT AuthorSeqNo, Name, Surname FROM Author NATURAL JOIN (SELECT * FROM Book_Author WHERE ISBN = ? ORDER BY AuthorSeqNo) AS BookAuthor;";
				stmt = conn.prepareStatement(query);
				stmt.setInt(1, book.ISBN);
				res = stmt.executeQuery();

				while (res.next()) {
					Author auth = new Author();
					auth.Name = res.getString("Name").trim();
					auth.Surname = res.getString("Surname").trim();
					book.authors.add(auth);
				}

				result += book.toFullString();
			}

			return result;

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(dialogParent, e.getMessage(),
					"Database Error", JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
			return "";
		} finally {
			try {
				res.close();
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public String showAuthor(int authorID) {

		try {

			String result = "Show Author\r\n\r\n";

			String query = "SELECT * FROM Author WHERE AuthorId = ?;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, authorID);
			res = stmt.executeQuery();

			if (!res.isBeforeFirst()) {
				return result + "No Results.\r\n";
			}

			res.next();
			
			Author author = new Author();
			
			author.AuthorId = res.getInt("AuthorId");
			author.Name = res.getString("Name").trim();
			author.Surname = res.getString("Surname").trim();

			query = "SELECT Book_Author.ISBN, Title FROM Book_Author, Book WHERE AuthorId = ? AND Book.ISBN = Book_Author.ISBN;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, author.AuthorId);
			res = stmt.executeQuery();

			while (res.next()) {
				Book book = new Book();
				book.ISBN = res.getInt("ISBN");
				book.Title = res.getString("Title");
				author.booksAuthored.add(book);
			}

			result += author.toFullString() + "\r\n";

			return result;

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(dialogParent, e.getMessage(),
					"Database Error", JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
			return "";
		} finally {
			try {
				res.close();
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public String showAllAuthors() {
		return "Show All Authors Stub";
	}

	public String showCustomer(int customerID) {
		return "Show Customer Stub";
	}

	public String showAllCustomers() {
		return "Show All Customers Stub";
	}

	public String borrowBook(int isbn, int customerID, int day, int month,
			int year) {
		return "Borrow Book Stub";
	}

	public String returnBook(int isbn, int customerid) {
		return "Return Book Stub";
	}

	public void closeDBConnection() {
	}

	public String deleteCus(int customerID) {
		return "Delete Customer";
	}

	public String deleteAuthor(int authorID) {
		return "Delete Author";
	}

	public String deleteBook(int isbn) {
		return "Delete Book";
	}
}