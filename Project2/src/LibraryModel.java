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

			String result = "Author\r\n\r\n";

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
		try {

			String result = "All Authors\r\n\r\n";

			String query = "SELECT * FROM Author;";
			stmt = conn.prepareStatement(query);
			res = stmt.executeQuery();

			if (!res.isBeforeFirst()) {
				return result + "No Results.\r\n";
			}
			
			List<Author> authors = new ArrayList<>();
			
			while (res.next()) {
				Author author = new Author();
				
				author.AuthorId = res.getInt("AuthorId");
				author.Name = res.getString("Name").trim();
				author.Surname = res.getString("Surname").trim();
				authors.add(author);
			}
			
			stmt.close();
			res.close();
			
			for(Author author : authors){
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

	public String showCustomer(int customerID) {

		try {

			String result = "Customer\r\n\r\n";

			String query = "SELECT * FROM Customer WHERE CustomerId = ?;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, customerID);
			res = stmt.executeQuery();

			if (!res.isBeforeFirst()) {
				return result + "No Results.\r\n";
			}

			res.next();
			
			Customer customer = new Customer();
			
			customer.customerID = res.getInt("CustomerId");
			customer.f_name = res.getString("f_name").trim();
			customer.l_name = res.getString("l_name").trim();
			customer.city = res.getString("City");
			
			//There isn't always a city, so check if there is, and trim if it exists..
			if(customer.city != null){
				customer.city = customer.city.trim();
			}

			query = "SELECT Book.ISBN, Book.Title FROM Book, Cust_Book WHERE CustomerId = ? AND Book.ISBN = Cust_Book.ISBN;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, customer.customerID);
			res = stmt.executeQuery();

			while (res.next()) {
				Book book = new Book();
				book.ISBN = res.getInt("ISBN");
				book.Title = res.getString("Title");
				customer.borrowing.add(book);
			}

			result += customer.toFullString() + "\r\n";

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

	public String showAllCustomers() {
		try {

			String result = "All Customers\r\n\r\n";

			String query = "SELECT * FROM Customer;";
			stmt = conn.prepareStatement(query);
			res = stmt.executeQuery();

			if (!res.isBeforeFirst()) {
				return result + "No Results.\r\n";
			}
			
			List<Customer> customers = new ArrayList<>();
			
			while(res.next()){
			
				Customer customer = new Customer();
			
				customer.customerID = res.getInt("CustomerId");
				customer.f_name = res.getString("f_name").trim();
				customer.l_name = res.getString("l_name").trim();
				customer.city = res.getString("City");
			
				//There isn't always a city, so check if there is, and trim if it exists..
				if(customer.city != null){
						customer.city = customer.city.trim();
				}
				
				customers.add(customer);
	
			}
	
			for(Customer customer : customers){
			
				query = "SELECT Book.ISBN, Book.Title FROM Book, Cust_Book WHERE CustomerId = ? AND Book.ISBN = Cust_Book.ISBN;";
				stmt = conn.prepareStatement(query);
				stmt.setInt(1, customer.customerID);
				res = stmt.executeQuery();
	
				while (res.next()) {
					Book book = new Book();
					book.ISBN = res.getInt("ISBN");
					book.Title = res.getString("Title");
					customer.borrowing.add(book);
				}
	
				result += customer.toFullString() + "\r\n";
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
		try {

			String result = "Delete Customer:\r\n\r\n";

			String query = "DELETE FROM Customer WHERE CustomerId = ?;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, customerID);
			stmt.executeUpdate();

			result += "\r\n\t Successfully deleted customer " + customerID;
			
			return result;

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(dialogParent, e.getMessage(),
					"Database Error: Deletion failed", JOptionPane.ERROR_MESSAGE);
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

	public String deleteAuthor(int authorID) {
		try {

			String result = "Delete Author:\r\n\r\n";

			String query = "DELETE FROM Author WHERE AuthorId = ?;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, authorID);
			stmt.executeUpdate();

			result += "\r\n\t Successfully deleted Author " + authorID;
			
			return result;

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(dialogParent, e.getMessage(),
					"Database Error: Deletion failed", JOptionPane.ERROR_MESSAGE);
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

	public String deleteBook(int isbn) {
		try {

			String result = "Delete Book:\r\n\r\n";

			String query = "DELETE FROM Book WHERE ISBN = ?;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, isbn);
			stmt.executeUpdate();

			result += "\r\n\t Successfully deleted book with ISBN: " + isbn;
			
			return result;

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(dialogParent, e.getMessage(),
					"Database Error: Deletion failed", JOptionPane.ERROR_MESSAGE);
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
}