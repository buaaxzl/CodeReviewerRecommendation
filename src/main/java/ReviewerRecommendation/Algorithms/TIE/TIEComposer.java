package ReviewerRecommendation.Algorithms.TIE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tools.SortMapElement;
import ReviewerRecommendation.ReReEntrance;
import ReviewerRecommendation.Algorithms.RecommendAlgo;

public class TIEComposer implements RecommendAlgo{
	
	private double alpha = 0.3;
	private final Classifier nb = new Classifier();
	private final AnotherFPS fps = new AnotherFPS();
	@Override
	public List<String> recommend(int i, ReReEntrance ent) {
		if (ent.getDataService().getCodeReviewers(i) == null
				|| ent.getDataService().getCodeReviewers(i).size() == 0)
			return null;
		
		List<String> result = nb.recommend(i, ent);
		fps.recommend(i, ent);
		
		Map<String, Double> confScore = nb.getConfScore();
		Map<String, Double> reviewers = fps.getReviewers();
		
		if (reviewers.size() == 0)
			return result;
					
		double totalConfText = 0.0;
		for (Map.Entry<String, Double> each : confScore.entrySet()) {
			totalConfText += each.getValue();
		}
		
		double totalConfPath = 0.0;
		for (Map.Entry<String, Double> each : reviewers.entrySet()) {
			totalConfPath += each.getValue();
		}

		Set<String> allReviewers = new HashSet<String>();
		allReviewers.addAll(confScore.keySet());
		allReviewers.addAll(reviewers.keySet());
		
		Map<String, Double> score = new HashMap<String, Double>();
		for (String rev : allReviewers) {
			double a, b;
			if (confScore.containsKey(rev)) a = confScore.get(rev);
			else a = 0.0;
			if (reviewers.containsKey(rev)) b = reviewers.get(rev);
			else b = 0.0;
			
			double tmp = 0.0;
			tmp = alpha * a / totalConfText +
					(1 - alpha) * b / totalConfPath;
			score.put(rev, tmp);
		}

		List<Map.Entry<String, Double>> entryList = SortMapElement.sortDouble(score);
		String author = ent.getDataService().getPrList().get(i).getUser().getLogin();
		int upper = ent.k;
		List<String> ret = new ArrayList<String>();
		for (int j = 0 ; j < upper; j++)
			if (j < entryList.size()) {
				if (!author.equals(entryList.get(j).getKey()))
					ret.add(entryList.get(j).getKey());
				else
					upper ++;
			}
			else
				break;
		
		return ret;
	}
}
