package clj.javapoly;

import java.util.List;

public interface RowInfo {
	public List<String> getFldNames();
	public int getFldCount();
	public String get(int index);

}
