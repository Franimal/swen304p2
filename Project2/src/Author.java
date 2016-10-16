import java.util.ArrayList;
import java.util.List;


public class Author {

	public int AuthorId;
	public String Name;
	public String Surname;
	public List<Book> booksAuthored;

	public Author(){
		this.booksAuthored = new ArrayList<Book>();
	}

	public String initialAndLastName(){
		return Name.charAt(0) + ". " + Surname; 
	}

	public String toFullString(){
		String result = toShortString();
		if(booksAuthored != null && !booksAuthored.isEmpty()){
			result += "\tBooks Authored:\r\n";
			for(Book b: booksAuthored){
				result += "\t" + b.toShortString();
			}
		}
		return result;
	}

	public String toShortString(){
		return String.format("\t%d - %s, %s\r\n", AuthorId, Surname, Name);
	}

}
