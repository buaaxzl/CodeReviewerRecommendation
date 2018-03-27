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
import edu.udo.cs.wvtool.generic.vectorcreation.TermOccurrences;


public class Classifier implements RecommendAlgo{
	private boolean isBuild = false; 
	
	public List<String> wordList;
	public Map<String, Map<Integer, Integer>> wordVector = new HashMap<String, Map<Integer, Integer>>();
	private Integer totalPR = 0;
	private Map<String, Double> confScore;
	
	public Map<String, Double> getConfScore() {
		return confScore;
	}
	
	public Map<String, Map<Integer, Integer>> termFrequencyPerReviewer = 
										new HashMap<String, Map<Integer, Integer>>();
	public Map<String, Set<String>> reviewerToPullRequest = 
										new HashMap<String, Set<String>>();
	public Map<String, Double> priorProb = new HashMap<String, Double>();
	public Map<String, Integer> totalFrequencyPerReviewer = 
									new HashMap<String, Integer>();
	public List<String> allReviewers = new ArrayList<String>();
	
	private void doStatistics(int index, ReReEntrance ent) {
		
		for (int i = 0; i < index; i++) {
			String number = ent.getDataService().getPrList().get(i).getNumber()+"";
			List<String> reviewers = ent.getDataService().getCodeReviewers(i);
			if (reviewers == null) continue;
			
			for (String rev : reviewers) {
				if (!allReviewers.contains(rev)) allReviewers.add(rev);
				
				if (reviewerToPullRequest.containsKey(rev))
					reviewerToPullRequest.get(rev).add(number);
				else {
					Set<String> tmp = new HashSet<String>();
					tmp.add(number);
					reviewerToPullRequest.put(rev, tmp);
				}
			}
			totalPR++;
		}
		
		for (Map.Entry<String, Set<String>> each : reviewerToPullRequest.entrySet()) {
			Map<Integer, Integer> tmp = new HashMap<Integer, Integer>();
			for (String pr : each.getValue()) {
				Map<Integer, Integer> m = wordVector.get(pr);
				for (Map.Entry<Integer, Integer> element : m.entrySet())
					if (tmp.containsKey(element.getKey()))
						tmp.put(element.getKey(), tmp.get(element.getKey()) + element.getValue());
					else
						tmp.put(element.getKey(), element.getValue());
			}
			termFrequencyPerReviewer.put(each.getKey(), tmp);
		}
		
		for (Map.Entry<String, Set<String>> each : reviewerToPullRequest.entrySet()) {
			priorProb.put(each.getKey(), each.getValue().size()/(totalPR+0.0));
		}
		
		for (Map.Entry<String, Map<Integer, Integer>> each : termFrequencyPerReviewer.entrySet()) {
			int val = 0;
			for (Map.Entry<Integer, Integer> t : each.getValue().entrySet()) 
				val += t.getValue();
			totalFrequencyPerReviewer.put(each.getKey(), val);
		}
	}

	public void modelConstruct(int index, ReReEntrance ent) throws Exception{
		try {
			WVToolProcess wvtp = new WVToolProcess(new TermOccurrences());
			wvtp.process("pr", ent.getDataService().getPrList().size(), ent.getDataService());
			wordList = wvtp.getWords();
			Map<String, Map<Integer, Double>> wv = wvtp.getWordVector();
			
			for (Map.Entry<String, Map<Integer, Double>> each : wv.entrySet()) {
				Map<Integer, Double> vals = each.getValue();
				Map<Integer, Integer> tmp = new HashMap<Integer, Integer>();
				for (Map.Entry<Integer, Double> val : vals.entrySet())
					tmp.put(val.getKey(), val.getValue().intValue());
				wordVector.put(each.getKey(), tmp);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		doStatistics(index, ent);
	}
	
	@Override
	public List<String> recommend(int i, ReReEntrance ent) {
		if (!isBuild) {
			isBuild = true;
			try {
				modelConstruct(i, ent);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		List<String> ret;
		ret = doRecommend(i, ent);

		updateModel(i, ent);
		return ret;
	}

	private void updateModel(int i, ReReEntrance ent) {
		//update allReviewers
		List<String> trueReviewers = ent.getDataService().getCodeReviewers(i);
		if (trueReviewers == null || trueReviewers.size() == 0) return;
		for (String each : trueReviewers)
			if (!allReviewers.contains(each))
				allReviewers.add(each);
		
		//update reviewerToPullRequest,termFrequencyPerReviewer,
		//       priorProb,totalFrequencyPerReviewer
		String number = ent.getDataService().getPrList().get(i).getNumber()+"";
		Map<Integer, Integer> termFrequency = wordVector.get(number);
		int freq = 0;
		for (Map.Entry<Integer, Integer> num : termFrequency.entrySet()) 
			freq += num.getValue();
		
		for (String rev : trueReviewers) {
			if (reviewerToPullRequest.containsKey(rev))
				reviewerToPullRequest.get(rev).add(number);
			else {
				Set<String> tmp = new HashSet<String>();
				tmp.add(number);
				reviewerToPullRequest.put(rev, tmp);
			}
		}
		for (String rev : trueReviewers) {
			if (!termFrequencyPerReviewer.containsKey(rev)) {
				termFrequencyPerReviewer.put(rev, termFrequency);
				totalFrequencyPerReviewer.put(rev, freq);
			}
			else {
				Map<Integer, Integer> old = termFrequencyPerReviewer.get(rev);
				for (Map.Entry<Integer, Integer> ele : termFrequency.entrySet())
					if (!old.containsKey(ele.getKey()))
						old.put(ele.getKey(), ele.getValue());
					else
						old.put(ele.getKey(), old.get(ele.getKey()) + ele.getValue());
				
				termFrequencyPerReviewer.put(rev, old);
				totalFrequencyPerReviewer.put(rev, totalFrequencyPerReviewer.get(rev) + freq);
			}
		}
		
		totalPR++;
		for (Map.Entry<String, Set<String>> each : reviewerToPullRequest.entrySet()) {
			priorProb.put(each.getKey(), each.getValue().size()/(totalPR+0.0));
		}
	}

	private List<String> doRecommend(int i, ReReEntrance ent) {
		String number = ent.getDataService().getPrList().get(i).getNumber()+"";
		
		Map<Integer, Integer> termFrequency = wordVector.get(number);
		
		int v = wordList.size();
		
		confScore = new HashMap<String, Double>();
		
		for (String reviewer : allReviewers) {
			confScore.put(reviewer, priorProb.get(reviewer));
			
			for (Map.Entry<Integer, Integer> t : termFrequency.entrySet()) {
				if (termFrequencyPerReviewer.get(reviewer).containsKey(t.getKey())) {
					int numerator = termFrequencyPerReviewer.get(reviewer).
											get(t.getKey()) + 1;
					int denominator = totalFrequencyPerReviewer.get(reviewer) + v;
					confScore.put(reviewer, confScore.get(reviewer) * (
											(numerator/(denominator+0.0))));
				}
				else
					confScore.put(reviewer, confScore.get(reviewer) * (
															(1/(v+0.0))));
			}
		}
		
		List<Map.Entry<String, Double>> entryList = SortMapElement.sortDouble(confScore);
		
		String author = ent.getDataService().getPrList().get(i).getUser().getLogin();
		int upper = ent.k;
		List<String> ret = new ArrayList<String>();
		for (int j = 0 ; j < upper; j++) {
			if (j < entryList.size()) {
				if (!author.equals(entryList.get(j).getKey()))
					ret.add(entryList.get(j).getKey());
				else
					upper++;
			} else break;
		}
		return ret;
	}
	
	public static void main(String[] args) throws Exception {
		Classifier t = new Classifier();
		ReReEntrance ent = new ReReEntrance("netty", "netty", "windows");
		t.recommend(ent.getDataService().getPrList().size()-2, ent);
		
		System.out.println(t.wordList.size());
		System.out.println(t.wordVector.get("1018").get(166));
	}
}
