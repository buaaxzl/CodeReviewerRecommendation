package ReviewerRecommendation.Algorithms.RSVD.recommend;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import ReviewerRecommendation.Algorithms.RSVD.models.BlenderWithOutImplicitFeedBack;
import ReviewerRecommendation.Algorithms.RSVD.RatingMatrix;
import ReviewerRecommendation.Algorithms.RSVD.train.Trainer;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.PullRequest;

import tools.SortMapElement;
import ReviewerRecommendation.ReReEntrance;
import ReviewerRecommendation.Algorithms.RecommendAlgo;
import ReviewerRecommendation.Algorithms.FPS.CommonPart;

public class SVDRecommendationFile implements RecommendAlgo{
	private boolean isBuild = false;
	private List<String> fileList;
	private List<String> reviewerList;
	
	private BlenderWithOutImplicitFeedBack blender;
	
	private Map<Integer, List<String>> prFileName;
	private RatingMatrix mat;
	
	class Tuple {
		public String file;
		public double simScore;
		public Tuple(String file, double simScore) 
		{this.file = file; this.simScore = simScore;}
	}
	
	@Override
	public List<String> recommend(int cur, ReReEntrance ent) {
		if (!isBuild) {
			isBuild = true;
			mat = constructMatrix(cur, ent);

			double lamdaValue = 0.05, stepValue = 0.0008;
			int kValue = 30;
			blender = new BlenderWithOutImplicitFeedBack(ent);
			blender.setK(kValue);
			blender.setLamda(lamdaValue);
			
			Trainer trainer = new Trainer(mat, null);
			trainer.setStep(stepValue);
			trainer.setModel(blender);
			trainer.setMat(mat);
			trainer.setTimes(25);
			trainer.setRatio(0.0001);
			trainer.train();
		}
		
		Map<String, Double> scores = new HashMap<String, Double>();
		List<String> files = prFileName.get(cur);
		List<String> lossFileCompensation = new ArrayList<String>();
		
		for (String file : files) {
			if (!fileList.contains(file)) {
				CommonPart cp = new CommonPart();
				
				PriorityQueue<Tuple> minHeap = new PriorityQueue<Tuple>(11, 
						new Comparator<Tuple>() {
							@Override
							public int compare(Tuple o1, Tuple o2) {
								return Double.compare(o1.simScore, o2.simScore);
							}
				});
				
				for (String eachFile : fileList) {
					if (files.contains(eachFile)) continue;
					
					double sim = cp.similar(file, eachFile);
					if (minHeap.size() < 1 || sim > minHeap.peek().simScore) {
						minHeap.offer(new Tuple(eachFile, sim));
						if (minHeap.size() > 1) 
							minHeap.poll();
					}
				}
				
				for (Tuple t : minHeap) {
					if (!lossFileCompensation.contains(t.file))
						lossFileCompensation.add(t.file);
				}
			}
		}
		
		for (int i = 0; i < reviewerList.size(); i ++) {
			double score = 0.0;
			for (String file : files) {
				if (!fileList.contains(file))
					continue;
				int j = fileList.indexOf(file);
				if (mat.isExist(i, j))
					score += mat.getVal(i, j);
				else
					score += blender.predictVal(i, j);
			}
			scores.put(reviewerList.get(i), score);
		}
		
		for (int i = 0; i < reviewerList.size(); i ++) {
			double score = 0.0;
			for (String file : lossFileCompensation) {
				int j = fileList.indexOf(file);
				if (mat.isExist(i, j))
					score += mat.getVal(i, j);
				else
					score += blender.predictVal(i, j);
			}
			scores.put(reviewerList.get(i), scores.get(reviewerList.get(i)) + score);
		}
		
		List<Map.Entry<String, Double>> entryList = SortMapElement.sortDouble(scores);
		
		String author = ent.getDataService().getPrList().get(cur).getUser().getLogin();
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

	private RatingMatrix constructMatrix(int cur, ReReEntrance ent) {
		
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
		
		Set<String> allFiles = new HashSet<String>();
		for (Map.Entry<Integer, List<String>> each : prFileName.entrySet())
			if (each.getKey() < cur && each.getValue() != null)
				allFiles.addAll(each.getValue());
		fileList = new ArrayList<String>(allFiles);
		
		Map<String, Set<Integer>> reviewerToPRs = new HashMap<String, Set<Integer>>();
		Set<String> allReviewers = new HashSet<String>();
		for (int j = 0; j < cur; j++) {
			Set<String> tmp = new HashSet<String>();
			
			if (ent.getDataService().getPrList().get(j).getUser() != null) {
				String author = ent.getDataService().getPrList().get(j).getUser().getLogin();
				tmp.add(author);
			}
			List<String> revs = ent.getDataService().getCodeReviewers(j);
			for (String each : revs) {
				if (reviewerToPRs.containsKey(each))
					reviewerToPRs.get(each).add(j);
				else {
					Set<Integer> inner = new HashSet<Integer>();
					inner.add(j);
					reviewerToPRs.put(each, inner);
				}
			}
			tmp.addAll(revs);
			allReviewers.addAll(tmp);
		}
		reviewerList = new ArrayList<String>(allReviewers);
		
		Date startTime = ent.getDataService().getPrList().get(0).getCreatedAt();
		Date endTime = ent.getDataService().getPrList().get(cur).getCreatedAt();
		
		RatingMatrix retMat = new RatingMatrix(reviewerList, fileList);
		for (String rev : reviewerList) {
			if (!reviewerToPRs.containsKey(rev)) continue;
			int U = reviewerList.indexOf(rev);
			
			Set<Integer> prids = reviewerToPRs.get(rev);
			for (Integer id : prids) {
				if (id >= cur) continue;
				List<String> files = prFileName.get(id);
				if (files == null) continue;
				
				Date nowTime = ent.getDataService().getPrList().get(id).getCreatedAt();
				for (String file : files) {
					int I = fileList.indexOf(file);
					double old;
					if (retMat.isExist(U, I))
						old = retMat.getVal(U, I);
					else
						old = 0.0;
					retMat.setVal(U, I, old + (nowTime.getTime()+0.0 - startTime.getTime()+0.0)/
							(endTime.getTime()+0.0 - startTime.getTime()+0.0));
				}
			}
		}
		
		System.out.println("reviewers: " + retMat.getX() + " files:" + retMat.getY());
		
		return retMat;
	}
}
