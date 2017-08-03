package clj.javapoly;

import java.util.List;
import java.util.Vector;

public class VectorAdapter<T> implements RowInfo {
	private Vector<T> vec;
	
	public VectorAdapter(Vector<T> vec){
		this.vec = vec;
	}

	@Override
	public List<String> getFldNames() {
		List<String> result = new Vector<String>();
		for (int i = 0; i < vec.size(); i++){
			result.add(String.format("%d", i));
		}
		return result;
	}

	@Override
	public int getFldCount() {
		return vec.size();
	}

	@Override
	public String get(int index) {
		return vec.get(index).toString();
	}

}
