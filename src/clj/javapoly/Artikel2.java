package clj.javapoly;

import java.util.Arrays;
import java.util.List;

public class Artikel2 implements RowInfo{
	
	public int    artNr;
	public String bez;
	public float  preis;
	
	public Artikel2(int artNr, String bez, float preis) {
		this.artNr = artNr;
		this.bez = bez;
		this.preis = preis;
	}

	@Override
	public List<String> getFldNames() {
		return Arrays.asList("ArtNr", "Bez", "Preis", "davon MWSt");
	}

	@Override
	public int getFldCount() {
		return 4;
	}

	@Override
	public String get(int index) {
		switch (index) {
		case 0:
			return String.format("%d", artNr);
		case 1:
			return bez;
		case 2:
			return String.format("%.2f", preis);
		case 3:
			return String.format("%.2f", 0.19f*preis/1.19f);
		default:
			throw new IndexOutOfBoundsException();
		}
	}
}
