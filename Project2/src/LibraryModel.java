import java.sql.Connection;
import java.sql.Date;
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
				
				query = "SELECT * FROM Customer NATURAL JOIN Cust_Book;";
				stmt = conn.prepareStatement(query);
				res = stmt.executeQuery();
				
				while(res.next()){
					Customer cust = new Customer();
					
					cust.customerID = res.getInt("CustomerID");
					cust.f_name = res.getString("F_Name").trim();
					cust.l_name = res.getString("L_Name").trim();
					cust.city = res.getString("City").trim();
					
					book.borrowedBy.add(cust);
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
		
		
		try {
		
			String result = "Borrow Book\r\n\n";
		
			String cust_name = "";
			String book_title = "";
			
			//Begin our transaction
			conn.setAutoCommit(false);
			
			//Make sure customer exists, lock customer
			String query = "SELECT * FROM Customer WHERE CustomerId = ? FOR UPDATE";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1,  customerID);
			res = stmt.executeQuery();
			
			//If there are no results, rollback.
			if(!res.isBeforeFirst()){
				conn.rollback();
				return result + "\tCustomer " + customerID + " does not exist in database.\r\n";
			}
			
			res.next();
			String f_name = res.getString("f_name").trim();
			String l_name = res.getString("l_name").trim();
			cust_name = f_name + " " + l_name;
			
			//Make sure book exists, lock book
			query = "SELECT * FROM Book WHERE ISBN = ? FOR UPDATE";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1,  isbn);
			res = stmt.executeQuery();
			
			//If no results, rollback..
			if(!res.isBeforeFirst()){
				conn.rollback();
				return result + "\tBook " + isbn + " does not exist in database.\r\n";
			}
			
			res.next();
			book_title = res.getString("title").trim();
			
 			//Make sure book is available to loan
			int available = res.getInt("numLeft");
			if(available == 0){
				conn.rollback();
				return result + "\t There are no copies available of '" + book_title + "' (" + isbn + ").\r\n"; 
			}
			
			//Make sure this customer isn't already loaning this book out
			query = "SELECT * FROM Cust_Book WHERE CustomerId = ?;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1,  customerID);
			res = stmt.executeQuery();
			
			if(res.isBeforeFirst()){
				conn.rollback();
				return result = "\t Customer " + cust_name + "(" + customerID + ") already has item " + isbn + " on loan.\r\t";  
			}
			
			//Create new entry in Cust_Book
			String dueDate = String.format("%04d-%02d-%02d", year, month+1, day);
			Date date = Date.valueOf(dueDate);
			query = "INSERT INTO Cust_Book (CustomerId, DueDate, ISBN) VALUES (?, ?, ?);";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1,  customerID);
			stmt.setDate(2,  date);
			stmt.setInt(3,  isbn);
			
			int updateResult = stmt.executeUpdate();
			
			if(updateResult != 1){
				conn.rollback();
				return result + "\t Book could not be borrowed.  Try again, or contact the system administrator.";
			}
			
			//pause on confirmation screen for testing purposes
			JOptionPane.showMessageDialog(dialogParent, "Paused transaction.  Press OK to confirm.", "Paused", JOptionPane.OK_OPTION);
			
			//Set numLeft for book to numLeft -1 
			query = "UPDATE Book SET numLeft = numLeft - 1 WHERE ISBN = ?;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1,  isbn);
			
			updateResult = stmt.executeUpdate();
			
			if(updateResult != 1){
				conn.rollback();
				return result + "\t Book could not be borrowed.  Try again, or contact the system administrator.";
			}
			
			//commit transaction, and return result
			conn.commit();
			conn.setAutoCommit(true);
			result += "\t" + cust_name + " (" + customerID + ") successfully borrowed " + book_title + " (" + isbn + ").  Due back on " + dueDate;
			
			return result;
			
		} catch (SQLException e){
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

	public String returnBook(int isbn, int customerID) {
	
		
		try {
		
			String result = "Return Book\r\n\n";
		
			String cust_name = "";
			String book_title = "";
			
			//Begin our transaction
			conn.setAutoCommit(false);
			
			//Make sure customer exists, lock customer
			String query = "SELECT * FROM Customer WHERE CustomerId = ? FOR UPDATE";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1,  customerID);
			res = stmt.executeQuery();
			
			//If there are no results, rollback.
			if(!res.isBeforeFirst()){
				conn.rollback();
				return result + "\tCustomer " + customerID + " does not exist in database.\r\n";
			}
			
			res.next();
			String f_name = res.getString("f_name").trim();
			String l_name = res.getString("l_name").trim();
			cust_name = f_name + " " + l_name;
			
			//Make sure book exists, lock book
			query = "SELECT * FROM Book WHERE ISBN = ? FOR UPDATE";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1,  isbn);
			res = stmt.executeQuery();
			
			//If no results, rollback..
			if(!res.isBeforeFirst()){
				conn.rollback();
				return result + "\tBook " + isbn + " does not exist in database.\r\n";
			}
			
			res.next();
			book_title = res.getString("title").trim();
			
 			//Make sure book is on loan
			int available = res.getInt("numLeft");
			int totalCopies = res.getInt("NumOfCop");
			if(available >= totalCopies){
				conn.rollback();
				return result + "\t There are no copies of '" + book_title + "' (" + isbn + ") on loan.  Stopping transaction.\r\n"; 
			}
			
			//Make sure the customer is loaning this book out
			query = "SELECT * FROM Cust_Book WHERE CustomerId = ?;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1,  customerID);
			res = stmt.executeQuery();
			
			if(!res.isBeforeFirst()){
				conn.rollback();
				return result = "\t Customer " + cust_name + "(" + customerID + ") does not have item " + isbn + " on loan.\r\t";  
			}
			
			//Delete entry from Cust_Book
			query = "DELETE FROM Cust_Book WHERE CustomerId = ?;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1,  customerID);
			
			int updateResult = stmt.executeUpdate();
			
			if(updateResult != 1){
				conn.rollback();
				return result + "\t Book could not be returned.  Try again, or contact the system administrator.";
			}
			
			//pause on confirmation screen for testing purposes
			JOptionPane.showMessageDialog(dialogParent, "Paused transaction.  Press OK to confirm book return.", "Paused", JOptionPane.OK_OPTION);
			
			//Set numLeft for book to numLeft + 1 
			query = "UPDATE Book SET numLeft = numLeft + 1 WHERE ISBN = ?;";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1,  isbn);
			
			updateResult = stmt.executeUpdate();
			
			if(updateResult != 1){
				conn.rollback();
				return result + "\t Book could not be returned.  Try again, or contact the system administrator.";
			}
			
			//commit transaction, and return result
			conn.commit();
			conn.setAutoCommit(true);
			result += "\t" + cust_name + " (" + customerID + ") successfully returned " + book_title + " (" + isbn + ").";
			
			return result;
			
		} catch (SQLException e){
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

	public void closeDBConnection() {
		try{
			conn.close();
			System.out.println("Closed database connection.");
		} catch (SQLException e){
			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Deletes a customer from the database.  Should only delete if they are not currently borrowing any books.  TODO
	 * @param customerID
	 * @return
	 */
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

	/**
	 * Deletes an author from the database.  Should only delete if there are no books in the DB written by them.  TODO
	 * @param authorID
	 * @return
	 */
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

	/**
	 * Deletes a book from the database.  Should only delete if noone is currently borrowing this book.  TODO
	 * @param isbn
	 * @return
	 */
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
