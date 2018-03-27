package ReviewerRecommendation.Algorithms.FPS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.PullRequest;

import tools.SortMapElement;
import ReviewerRecommendation.ReReEntrance;
import ReviewerRecommendation.Algorithms.RecommendAlgo;

public class FPS implements RecommendAlgo {

	public Map<Integer, List<String>> prFileName = new HashMap<Integer, List<String>>();
	
	public Map<String, Double> reviewers = null;
	public Map<String, Double> getReviewers() {
		return reviewers;
	}

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
		
		List<String> reviewers1 = getCandidateReviewers(i, ent, new LCP());

		/*
		List<String> reviewers2 = getCandidateReviewers(i, ent, new LCSuff());
		List<String> reviewers3 = getCandidateReviewers(i, ent, new LCSubstr());
		List<String> reviewers4 = getCandidateReviewers(i, ent, new LCSubseq());
		*/
		
		List<String> revs = bordaCount(i, ent, reviewers1);
		return revs;
	}
	
	public void preProcess(ReReEntrance ent) {
		for (int i = 0; i < ent.getDataService().getPrList().size(); i++) {
			PullRequest pr = ent.getDataService().getPrList().get(i);
			String number = pr.getNumber()+"";
			List<CommitFile> files = ent.getDataService().getDp().getPrFiles().get(number);
			
			if (files == null) {
				prFileName.put(i, null);
				continue;
			}
			
			List<String> filenames = new ArrayList<String>();

			for (CommitFile each : files) {
				if (each.getFilename() == null) {
                    continue;
                }
				if (!filenames.contains(each.getFilename())) {
                    filenames.add(each.getFilename());
                }
			}
			prFileName.put(i, filenames);
		}
	}

	public List<String> getCandidateReviewers(int rn, ReReEntrance ent, FilePathComparator fpc) {
		
		List<String> filesn = getFiles(rn);
		
		int beginIndex = 0;
		beginIndex = ent.temporalLocality(rn);

		for (int i = beginIndex; i < rn; i++) {
			if (!validatePR(i, ent)) {
                continue;
            }
			reviewerExpertise(i, filesn, ent, fpc);
		}
		
		List<Map.Entry<String, Double>> entryList = SortMapElement.sortDouble(reviewers);
		
		List<String> ret = new ArrayList<String>();
		for (Map.Entry<String, Double> each : entryList) 
			ret.add(each.getKey());
		
		return ret;
	}
	
	public void reviewerExpertise(int i, List<String> filesn, ReReEntrance ent,
			FilePathComparator fpc) {
		List<String> filesp = getFiles(i);
		
		double scorep = 0.0;
		for (String a : filesn)
			for (String b : filesp)
				scorep = scorep + fpc.similar(a, b);

		scorep = scorep / (filesn.size() * filesp.size());
		
		List<String> revs = ent.getDataService().getCodeReviewers(i);
		for (String reviewer : revs) {
			if (reviewers.containsKey(reviewer))
				reviewers.put(reviewer, reviewers.get(reviewer)+scorep);
			else
				reviewers.put(reviewer, scorep);
		}
	}
	
	public boolean validatePR(int index, ReReEntrance ent) {
		List<String> filesp = getFiles(index);
		if (filesp == null || filesp.size() == 0) 
			return false;
		
		if (ent.getDataService().getCodeReviewers(index) == null ||
				ent.getDataService().getCodeReviewers(index).size() == 0)
			return false;
		return true;
	}
	
	public List<String> getFiles(int i) {
		return prFileName.get(i);
	}
	
	public List<String> bordaCount(int cur, ReReEntrance ent, @SuppressWarnings("unchecked") List<String>... r) {
		Map<String, Integer> total = new HashMap<String, Integer>();
		
		for (int i = 0; i < r.length; i++) {
			calculateRankScore(total, r[i]);
		}
		
		List<Map.Entry<String, Integer>> entryList = SortMapElement.sortInteger(total);
		
		String author = ent.getDataService().getPrList().get(cur).getUser().getLogin();
		
		int upper = ent.k;
		List<String> ret = new ArrayList<String>();
		for (int i = 0 ; i < upper; i++) {
            if (i < entryList.size()) {
                if (!author.equals(entryList.get(i).getKey()))
                    ret.add(entryList.get(i).getKey());
                else
                    upper++;
            } else
                break;
        }
		return ret;
	}
	
	public void calculateRankScore(Map<String, Integer> total, List<String> r) {
		int m = r.size();
		for (int i = 0; i < m; i++) 
			if (!total.containsKey(r.get(i))) 
				total.put(r.get(i), i);
			else
				total.put(r.get(i), total.get(r.get(i))+i);
	}
}
