package clj.javapoly;

public class Kunde1 {
	public int    kndnr;
	public String name;
	public String vorname;
	
	public Kunde1(int kndnr, String name, String vorname) {
		this.kndnr = kndnr;
		this.name = name;
		this.vorname = vorname;
	}

	public String toHtml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<tr>");
		sb.append("<td>"); sb.append(String.format("%d", kndnr)); sb.append("</td>");
		sb.append("<td>"); sb.append(name); sb.append("</td>");
		sb.append("<td>"); sb.append(vorname); sb.append("</td>");
		sb.append("</tr>");
		return sb.toString();
	}
	
}
	