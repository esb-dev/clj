package clj.javapoly;

import java.util.List;
import java.util.Vector;

public class Poly1 {
	
	public static List<Kunde1> getTestData() {
		Vector<Kunde1> data = new Vector<Kunde1>();
		data.add(new Kunde1(1, "Schneider", "Hans"));
		data.add(new Kunde1(2, "Henning", "Christa"));
		data.add(new Kunde1(3, "Berendt", "Uwe"));
		return data;
		
	}
	
	public static void printHtmlTable(List<Kunde1> lk) {
		System.out.println("<table>");
		System.out.println("<tr><th>Kndnr</th><th>Name</th><th>Vorname</th></tr>");
		for (Kunde1 k: lk) {
			System.out.println(k.toHtml());
		}
		System.out.println("</table>");
	}

	public static void main(String[] args) {
		printHtmlTable(getTestData());
	}

}
