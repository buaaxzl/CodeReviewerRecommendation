package ReviewerRecommendation.Algorithms.TIE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.SortMapElement;
import ReviewerRecommendation.ReReEntrance;
import ReviewerRecommendation.Algorithms.FPS.CommonPart;
import ReviewerRecommendation.Algorithms.FPS.FPS;
import ReviewerRecommendation.Algorithms.FPS.FilePathComparator;

public class AnotherFPS extends FPS{

	@Override
	public List<String> recommend(int i, ReReEntrance ent) {
		reviewers = new HashMap<String, Double>();
		
		if (ent.getDataService().getCodeReviewers(i) == null
				|| ent.getDataService().getCodeReviewers(i).size() == 0)
			return null;
		
		if (prFileName.size() == 0) 
			preProcess(ent);
		
		if (getFiles(i) == null || getFiles(i).size() == 0)
			return null;
		
		List<String> reviewers1 = getCandidateReviewers(i, ent, new CommonPart());
		
		List<String> reviewers = bordaCount(i, ent, reviewers1);
		return reviewers;
	}
	
	public List<String> getCandidateReviewers(int rn, ReReEntrance ent, FilePathComparator fpc) {
		
		List<String> filesn = getFiles(rn);
		
		int beginIndex = 0;
		beginIndex = ent.temporalLocality(rn);

		for (int i = beginIndex; i < rn; i++) {
			if (!validatePR(i, ent))
				continue;
			reviewerExpertise(i, filesn, ent, fpc);
		}
		
		List<Map.Entry<String, Double>> entryList = SortMapElement.sortDouble(reviewers);
		
		List<String> ret = new ArrayList<String>();
		for (Map.Entry<String, Double> each : entryList) 
			ret.add(each.getKey());
		
		return ret;
	}
}
