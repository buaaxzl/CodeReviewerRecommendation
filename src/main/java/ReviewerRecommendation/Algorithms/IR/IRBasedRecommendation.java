package ReviewerRecommendation.Algorithms.IR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.SortMapElement;
import ReviewerRecommendation.ReReEntrance;
import ReviewerRecommendation.Algorithms.RecommendAlgo;
import ReviewerRecommendation.Algorithms.TIE.WVToolProcess;
import edu.udo.cs.wvtool.generic.vectorcreation.WVTVectorCreator;
import edu.udo.cs.wvtool.main.WVTDocumentInfo;
import edu.udo.cs.wvtool.main.WVTWordVector;
import edu.udo.cs.wvtool.util.WVToolException;
import edu.udo.cs.wvtool.wordlist.WVTWordList;

class MyTFIDF implements WVTVectorCreator {

	@Override
	public WVTWordVector createVector(int[] frequencies, int numTermOccurences,
			WVTWordList wordList, WVTDocumentInfo d) throws WVToolException {
		// Obtain the total number of documents and the document frequencies
        int numDocuments = wordList.getNumDocuments();
        int[] docFrequencies = wordList.getDocumentFrequencies();

        // Create the result structure
        WVTWordVector result = new WVTWordVector();
        double[] wv = new double[docFrequencies.length];

        // Create the vector

        // If the document contains at least one term
        if (numTermOccurences > 0) {
            for (int i = 0; i < wv.length; i++) {

                // Note: docFrequencies[i] is always > 0 as otherwise the word
                // would not be in the word list, it is also always smaller as
                // the total number of documents

                double idf = Math.log(((double) numDocuments) / ((double) docFrequencies[i]));

                wv[i] = Math.log( (((double) frequencies[i]) / ((double) numTermOccurences)) + 1) * idf;

            }

        } else {
			for (int i = 0; i < wv.length; i++)
				wv[i] = 0.0;
		}

        result.setDocumentInfo(d);
        result.setValues(wv);
        return result;
	}
}

public class IRBasedRecommendation implements RecommendAlgo{

	private boolean isBuild = false;
	private List<String> wordList = new ArrayList<String>();
	private Map<String, Map<Integer, Double>> wordVector = new HashMap<String, Map<Integer, Double>>();
	private Map<String, Double> reviewersScore;
	
	
	public Map<String, Double> getReviewersScore() {
		return reviewersScore;
	}

	@Override
	public List<String> recommend(int i, ReReEntrance ent) {
		if (!isBuild) {
			isBuild = true;
			
			WVToolProcess wvtp = new WVToolProcess(new MyTFIDF());
			try {
				wvtp.process("pr", ent.getDataService().getPrList().size(), ent.getDataService());
				wordList = wvtp.getWords();
				wordVector = wvtp.getWordVector();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		reviewersScore = new HashMap<String, Double>();
		
		String prNumber = ent.getDataService().getPrList().get(i).getNumber()+"";
		
		int beginIndex = 0;
//		beginIndex = ent.temporalLocality(i);
		
		for (int j = beginIndex; j < i; j++) {
			String resolved = ent.getDataService().getPrList().get(j).getNumber()+"";
			List<String> revs = ent.getDataService().getCodeReviewers(j);
			double sim = cosineSimilarity(wordVector.get(prNumber), wordVector.get(resolved));
			
			for (String each : revs) {
				if (reviewersScore.containsKey(each))
					reviewersScore.put(each, reviewersScore.get(each) + sim);
				else
					reviewersScore.put(each, sim);
			}
		}
		

		List<Map.Entry<String, Double>> entryList = SortMapElement.sortDouble(reviewersScore);
		
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

	private double cosineSimilarity(Map<Integer, Double> a, Map<Integer, Double> b) {
		Double x = 0.0, y1 = 0.0, y2 = 0.0;
		for (Map.Entry<Integer, Double> each : a.entrySet()) {
			if (b.containsKey(each.getKey())) {
				x += each.getValue() * b.get(each.getKey());
			}
		}
		
		for (Map.Entry<Integer, Double> each : a.entrySet()) {
			y1 += each.getValue() * each.getValue();
		}
		for (Map.Entry<Integer, Double> each : b.entrySet()) {
			y2 += each.getValue() * each.getValue();
		}
		
		if (x.equals(0.0)) return 0.0;
		return  x/(Math.sqrt(y1) * Math.sqrt(y2));
	}
}
