package ReviewerRecommendation.Algorithms.MixedAlgo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ReviewerRecommendation.Algorithms.RecommendAlgo;
import tools.SortMapElement;
import ReviewerRecommendation.ReReEntrance;
import ReviewerRecommendation.Algorithms.Collaboration.CommentNetwork;
import ReviewerRecommendation.Algorithms.IR.IRBasedRecommendation;

public class IRandCN implements RecommendAlgo {

	private IRBasedRecommendation ir = new IRBasedRecommendation();
	private CommentNetwork cn = new CommentNetwork();
	
	public void setIr(IRBasedRecommendation ir) {
		this.ir = ir;
	}
	public void setCn(CommentNetwork cn) {
		this.cn = cn;
	}
	
	@Override
	public List<String> recommend(int i, ReReEntrance ent) {
		
		int Kval = ent.getK();
		ent.setK(50);
		
		ir.recommend(i, ent);
		cn.recommend(i, ent);
		
		ent.setK(Kval);
		
		Map<String, Double> scoreOfIR = ir.getReviewersScore();
		Map<String, Double> scoreOfCN = cn.getReviewersScore();
					
		double totalIR = 0.0;
		for (Map.Entry<String, Double> each : scoreOfIR.entrySet()) {
			totalIR += each.getValue();
		}
		
		double totalCN = 0.0;
		for (Map.Entry<String, Double> each : scoreOfCN.entrySet()) {
			totalCN += each.getValue();
		}
		
		Set<String> allReviewers = new HashSet<String>();
		allReviewers.addAll(scoreOfIR.keySet());
		allReviewers.addAll(scoreOfCN.keySet());
		
		Map<String, Double> score = new HashMap<String, Double>();
		for (String rev : allReviewers) {
			double a, b;
			if (scoreOfIR.containsKey(rev)) a = scoreOfIR.get(rev);
			else a = 0.0;
			if (scoreOfCN.containsKey(rev)) b = scoreOfCN.get(rev);
			else b = 0.0;
			
			double tmp = 0.0;
			
			if (totalIR > 0.0)
				tmp = 0.5 * a / totalIR;
			if (totalCN > 0.0)
				tmp += 0.5 * b / totalCN;
			
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
