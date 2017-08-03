package clj.javapoly;

import java.util.Arrays;
import java.util.List;

public class Kunde2 implements RowInfo{
	public int    kndNr;
	public String name;
	public String vorname;
	
	public Kunde2(int kndNr, String name, String vorname) {
		this.kndNr = kndNr;
		this.name = name;
		this.vorname = vorname;
	}

	@Override
	public List<String> getFldNames() {
		return Arrays.asList("KndNr", "Name", "Vorname");
	}

	@Override
	public int getFldCount() {
		return 3;
	}

	@Override
	public String get(int index) {
		switch (index) {
		case 0:
			return String.format("%d", kndNr);
		case 1:
			return name;
		case 2:
			return vorname;
		default:
			throw new IndexOutOfBoundsException();
		}
	}

}
	