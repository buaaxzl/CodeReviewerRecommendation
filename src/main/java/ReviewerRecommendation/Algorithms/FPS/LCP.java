package ReviewerRecommendation.Algorithms.FPS;

public class LCP implements FilePathComparator{

	@Override
	public double similar(String f1, String f2) {
		String[] f1Component = f1.split("/");
		String[] f2Component = f2.split("/");
		
		int border = f1Component.length > f2Component.length ? 
							f2Component.length : f1Component.length;
		int i = 0;
		for (; i < border; i++) {
			if (f1Component[i].equals(f2Component[i]))
				continue;
			else break;
		}
		int max = f1Component.length > f2Component.length ? 
				f1Component.length : f2Component.length;
		
		if (i == 0 || max == 0) return 0;
		return i / (max + 0.0);
	}
	
	public static void main(String[] args) {
		System.out.println(new LCP().similar("src/com/android/settings/UtilsLocationSettings.java",
				"src/com/android/settings/Utils.java"));

	}
}


