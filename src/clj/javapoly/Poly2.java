package clj.javapoly;

import java.util.List;
import java.util.Vector;

public class Poly2 {
	
	public static List<RowInfo> getTestKunden() {
		Vector<RowInfo> data = new Vector<RowInfo>();
		data.add(new Kunde2(1, "Schneider", "Hans"));
		data.add(new Kunde2(2, "Henning", "Christa"));
		data.add(new Kunde2(3, "Berendt", "Uwe"));
		return data;
	}
	
	public static List<RowInfo> getTestArtikel() {
		Vector<RowInfo> data = new Vector<RowInfo>();
		data.add(new Artikel2(1, "BitteEinBit", 1.20f));
		data.add(new Artikel2(2, "VinoVino", 9.90f));
		data.add(new Artikel2(3, "Hennessy", 24.95f));
		return data;
	}
	
	public static void printHtmlTable(List<RowInfo> l) {
		System.out.println("<table>");
		System.out.print("<tr>");
		for (String fldName: l.get(0).getFldNames()) {
			System.out.print("<th>" + fldName + "</th>");
		}	
		System.out.println("</tr>");
		for (RowInfo ri: l) {
			System.out.print("<tr>");
			for (int i = 0; i < ri.getFldCount(); i++) {
				System.out.print("<td>" + ri.get(i) + "</td>");
			}
			System.out.println("</tr>");
		}
		System.out.println("</table>");
	}

	public static void main(String[] args) {
		printHtmlTable(getTestKunden());
		System.out.println();
		printHtmlTable(getTestArtikel());
	}

}
