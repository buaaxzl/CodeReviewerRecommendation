package ReviewerRecommendation.Algorithms.FPS;

import java.util.HashSet;
import java.util.Set;

public class CommonPart implements FilePathComparator{

	public static void main(String[] args) {
	}

	@Override
	public double similar(String f1, String f2) {
		String[] f1Component = f1.split("/");
		String[] f2Component = f2.split("/");
		int len1 = f1Component.length;
		int len2 = f2Component.length;
		
		Set<String> f1Parts = new HashSet<String>();
		Set<String> f2Parts = new HashSet<String>();
		Set<String> allParts = new HashSet<String>();
		for (int i = 0; i < len1; i++) f1Parts.add(f1Component[i]);
		for (int i = 0; i < len2; i++) f2Parts.add(f2Component[i]);
		allParts.addAll(f1Parts);
		allParts.addAll(f2Parts);
		
		int common = 0;
		for (String each : allParts) {
			if (f1Parts.contains(each) && f2Parts.contains(each))
				common ++;
		}
		
		int max = len1 > len2 ? len1 : len2;
		if (common == 0 || max == 0) return 0;
		else return common / (max + 0.0);
	}
}
