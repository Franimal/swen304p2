import java.util.ArrayList;
import java.util.List;


public class Customer {
	public int customerID;
	public String f_name;
	public String l_name;
	public String city;
	public List<Book> borrowing;

	public Customer(){
		borrowing = new ArrayList<Book>();
	}

	public String toFullString(){
		String result = toShortString();
		result += "\tBorrowing:\r\n";
		if(borrowing.isEmpty()){
			result += "\t\t(None)\r\n";
		} else {
			for(Book b: borrowing){
				result += "\t" + b.toShortString() + "\r\n";
			}
		}
		return result;
	}

	public String toShortString(){
		return String.format("\t%d - %s, %s - %s\r\n", customerID, l_name, f_name, city != null ? city:"(No City)");
	}
}
