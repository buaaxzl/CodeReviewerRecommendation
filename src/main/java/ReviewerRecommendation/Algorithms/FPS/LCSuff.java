package ReviewerRecommendation.Algorithms.FPS;

public class LCSuff implements FilePathComparator{

	@Override
	public double similar(String f1, String f2) {
		String[] f1Component = f1.split("/");
		String[] f2Component = f2.split("/");
		
		int len1 = f1Component.length;
		int len2 = f2Component.length;
		int i = 0; 
		for (--len1, --len2; len1>=0 && len2>=0; --len1, --len2) {
			if (f1Component[len1].equals(f2Component[len2]))
				i++;
			else break;
		}
		
		int max = f1Component.length > f2Component.length ? 
				f1Component.length : f2Component.length;
		
		if (i == 0 || max == 0) return 0;
		return i / (max + 0.0);
	}
	
	public static void main(String[] args) {

		System.out.println(new LCSuff().similar("src/imports/undo/undo.pro",
				"tests/auto/undo/undo.pro"));
		
	}
}
