package ReviewerRecommendation.Algorithms.RSVD.recommend;

import ReviewerRecommendation.Algorithms.FPS.FilePathComparator;
import ReviewerRecommendation.Algorithms.FPS.LCP;
import ReviewerRecommendation.Algorithms.RSVD.models.BlenderWithOutImplicitFeedBack;
import ReviewerRecommendation.Algorithms.RSVD.RatingMatrix;
import ReviewerRecommendation.Algorithms.RSVD.train.Trainer;
import ReviewerRecommendation.Algorithms.RecommendAlgo;
import ReviewerRecommendation.ReReEntrance;
import ReviewerRecommendation.DataService.Pair;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.PullRequest;
import tools.SortMapElement;

import java.util.*;

public class SVDRecommendationPR implements RecommendAlgo{
	
	private boolean isBuild = false;
	private List<String> prList;
	private List<String> reviewerList;

	private BlenderWithOutImplicitFeedBack blender;

	private RatingMatrix mat;
	private int beginIndex;
	private int endIndex;
	private Map<Integer, List<String>> prFileName;
	
	private Map<String, Double> reviewerScores;
	
	public Map<String, Double> getReviewerScores() {
		return reviewerScores;
	}
	
	private void getPrFile(ReReEntrance ent) {
		prFileName = new HashMap<Integer, List<String>>();
		
		for (int j = 0; j < ent.getDataService().getPrList().size(); j++) {
			PullRequest pr = ent.getDataService().getPrList().get(j);
			String number = pr.getNumber()+"";
			List<CommitFile> files = ent.getDataService().getDp().getPrFiles().get(number);
			
			if (files == null) {
				prFileName.put(j, null);
				continue;
			}
			
			List<String> filenames = new ArrayList<String>();
			for (CommitFile each : files) {
				if (each.getFilename() == null)
					continue;
				if (!filenames.contains(each.getFilename()))
					filenames.add(each.getFilename());
			}
			prFileName.put(j, filenames);
		}
	}
	
	public double getSimilarity(int i, int cur, FilePathComparator fpc, ReReEntrance ent) {
		List<String> filesp = prFileName.get(i);
		List<String> filesn = prFileName.get(cur);
		
		double scorep = 0.0;
		for (String a : filesn)
			for (String b : filesp)
				scorep = scorep + fpc.similar(a, b);
		
		scorep = scorep / (filesn.size() * filesp.size());
		return scorep;
	}
	
	@Override
	public List<String> recommend(int cur, ReReEntrance ent) {
		if (!isBuild) {
			isBuild = true;
			getPrFile(ent);
			endIndex = cur;
			mat = constructMatrix(cur, ent);
			
			Map<String, Double> stepMap = new HashMap<String, Double>();
			stepMap.put("angular", 0.0001);
			stepMap.put("netty", 0.0005);
			stepMap.put("saltstack", 0.001);
			stepMap.put("ipython", 0.001);
			stepMap.put("symfony", 0.0005);
			
			double lamdaValue = 0.05;
			double stepValue = 0.001;
			
			stepValue = stepMap.get(ent.getOwner());
			
			int kValue = 20;
			blender = new BlenderWithOutImplicitFeedBack(ent);
			blender.setK(kValue);
			blender.setLamda(lamdaValue);
			
			Trainer trainer = new Trainer(mat, null);
			trainer.setStep(stepValue);
			trainer.setModel(blender);
			trainer.setMat(mat);
			trainer.setTimes(100);
			trainer.setRatio(0.0001);
			trainer.train();
		}
		
		if (prFileName.get(cur) == null || prFileName.get(cur).size() == 0)
			return null;
		
		reviewerScores = new HashMap<String, Double>();
		
		for (int i = beginIndex; i < endIndex; i++) {
			if (prFileName.get(i) == null || prFileName.get(i).size() == 0)
				continue;
			Double sim = getSimilarity(i, cur, new LCP(), ent);
			
			if (sim  < 0.3) continue;
			sim = 1.0;
			for (int j = 0; j < reviewerList.size(); j ++) {
				Double matrixElement = 0.0;

				if (mat.isExist(j, i-beginIndex)) {
					matrixElement = mat.getVal(j, i-beginIndex);
				}
				else {
					matrixElement = blender.predictVal(j, i-beginIndex);
				}
				
				
				if (reviewerScores.containsKey(reviewerList.get(j))) {
					reviewerScores.put(reviewerList.get(j), 
							reviewerScores.get(reviewerList.get(j)) + sim * matrixElement);
				}
				else {
					reviewerScores.put(reviewerList.get(j), sim * matrixElement);
				}
			}
		}
		
		List<Map.Entry<String, Double>> entryList = SortMapElement.sortDouble(reviewerScores);
		
		String author = ent.getDataService().getPrList().get(cur).getUser().getLogin();
		int upper = ent.k;
		List<String> ret = new ArrayList<String>();
		for (int j = 0 ; j < upper; j++) {
			if (j < entryList.size()) {
				if (!author.equals(entryList.get(j).getKey()))
					ret.add(entryList.get(j).getKey());
				else
					upper++;
			} else
				break;
		}
		
		return ret;
	}
	
	private RatingMatrix constructMatrix(int cur, ReReEntrance ent) {
		beginIndex = ent.temporalLocality(cur);

		List<PullRequest> prs = ent.getDataService().getPrList().subList(beginIndex, cur);
		prList = new ArrayList<String>();
		
		for (int i = 0; i < prs.size(); i++) {
			prList.add(ent.getDataService().getPrList().get(i + beginIndex).getNumber() + "");
		}
		
		Set<String> allReviewers = new HashSet<String>();
		for (int j = beginIndex; j < cur; j++) {
			List<String> revs = ent.getDataService().getCodeReviewers(j);
			allReviewers.addAll(revs);
		}
		reviewerList = new ArrayList<String>(allReviewers);
		
		RatingMatrix retMat = new RatingMatrix(reviewerList, prList);
		
		double endTime = ent.getDataService().getPrList().get(cur).getCreatedAt().getTime() + 0.0;
		double startTime = ent.getDataService().getPrList().get(beginIndex).getCreatedAt().getTime()+0.0 - 1000.0*3600.0;
		
		for (int i = 0; i < prList.size(); i++) {
			int origin = beginIndex + i;
			
			Map<String, List<Pair>> revs = ent.getDataService().getReviewerContribution(origin);
			for (Map.Entry<String, List<Pair>> rev : revs.entrySet()) {
				int U = reviewerList.indexOf(rev.getKey());
				double preTime;
				double val = 0.0;
				double decay = 0.7;
				
				List<Pair> pairs = rev.getValue();
				Collections.sort(pairs, new Comparator<Pair>() {
					@Override
					public int compare(Pair o1, Pair o2) {
						return o2.getDate().compareTo(o1.getDate());
					}
				});
				
				for (int r = 0; r < pairs.size(); r ++) {
					preTime = pairs.get(r).getDate().getTime()+0.0;
					val += Math.pow(decay, r) * (preTime - startTime) / (endTime - startTime);
				}
				retMat.setVal(U, i, val);
			}
		}
		
		System.out.println("reviewers: " + retMat.getX() + " pull requests: " + retMat.getY());
		
		return retMat;
	}
}
