package ReviewerRecommendation.Algorithms.FPS;

public class LCSubseq implements FilePathComparator {

	@Override
	public double similar(String f1, String f2) {
		String[] f1Component = f1.split("/");
		String[] f2Component = f2.split("/");
		
		int len1 = f1Component.length;
		int len2 = f2Component.length;
		int[][] mat = new int[len1][len2];
		
		for (int i = 0; i < len1; i++) {
			for (int j = 0; j < len2; j++) {
				if (f1Component[i].equals(f2Component[j])) {
					if (i-1>=0 && j-1>=0)
						mat[i][j] = mat[i-1][j-1] + 1;
					else
						mat[i][j] = 1;
				}
				else {
					if (i-1>=0 && j-1>=0)
						mat[i][j] = mat[i-1][j] > mat[i][j-1] ?
									mat[i-1][j] : mat[i][j-1];
					else
						mat[i][j] = 0;
				}
			}
		}
		
		int max = f1Component.length > f2Component.length ? 
				f1Component.length : f2Component.length;
		
		if (max == 0) return 0;
		return mat[len1-1][len2-1] / (max + 0.0);
	}
	
	public static void main(String[] args) {

		System.out.println(new LCSubseq().similar("src/imports/undo/undo.pro",
				"tests/auto/undo/undo.pro/imports/src/imports/undo/undo.pro"));
	}
}
