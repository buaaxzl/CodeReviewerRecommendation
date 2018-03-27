package ReviewerRecommendation.Algorithms.Activeness;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.SortMapElement;
import ReviewerRecommendation.ReReEntrance;
import ReviewerRecommendation.Algorithms.RecommendAlgo;

public class Activeness implements RecommendAlgo{

	private int temporalWindow = 60;
	private double lamda = 1;
	private boolean isBuild = false;
	private Map<String, List<Integer>> commenterPrs = new HashMap<String, List<Integer>>();
	
	@Override
	public List<String> recommend(int i, ReReEntrance ent) {
		if (!isBuild) {
			isBuild = true;
			for (int j = 0; j < i; j++) {
				List<String> revs = ent.getDataService().getCodeReviewers(j);
				for (String rev : revs) {
					if (commenterPrs.containsKey(rev))
						commenterPrs.get(rev).add(j);
					else {
						List<Integer> tmp = new ArrayList<Integer>();
						tmp.add(j);
						commenterPrs.put(rev, tmp);
					}
				}
			}
		}
		
		List<String> res = doRecommend(i, ent);
		update(i, ent);
		
		return res;
	}

	private void update(int i, ReReEntrance ent) {
		List<String> revs = ent.getDataService().getCodeReviewers(i);
		for (String rev : revs) {
			if (commenterPrs.containsKey(rev))
				commenterPrs.get(rev).add(i);
			else {
				List<Integer> tmp = new ArrayList<Integer>();
				tmp.add(i);
				commenterPrs.put(rev, tmp);
			}
		}
	}

	private List<String> doRecommend(int i, ReReEntrance ent) {
		Date timePnew = ent.getDataService().getPrList().get(i).getCreatedAt();
		Map<String, Double> scores = new HashMap<String, Double>();
		
		for (Map.Entry<String, List<Integer>> each : commenterPrs.entrySet()) {
			String reviewer = each.getKey();
			for (Integer prId : each.getValue()) {
				Date timePj = ent.getDataService().getPrList().get(prId).getCreatedAt();
				if (timeCheck(timePnew, timePj)) {
					if (scores.containsKey(reviewer)) {
						scores.put(reviewer, scores.get(reviewer) + 
								1.0/Math.pow((double)(timePnew.getTime()-timePj.getTime()), lamda));
					}
					else
						scores.put(reviewer,  
								1.0/Math.pow((double)(timePnew.getTime()-timePj.getTime()), lamda));
				}
			}
		}
		
		List<Map.Entry<String, Double>> entryList = SortMapElement.sortDouble(scores);
		
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

	private boolean timeCheck(Date timePnew, Date createdAt) {
		if (timePnew.getTime() - createdAt.getTime() <= (temporalWindow * 24 * 3600 * 1000L))
			return true;
		return false;
	}
}
