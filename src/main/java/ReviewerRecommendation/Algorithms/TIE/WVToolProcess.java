package ReviewerRecommendation.Algorithms.TIE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ReviewerRecommendation.DataService;
import org.eclipse.egit.github.core.Issue;

import edu.udo.cs.wvtool.config.WVTConfiguration;
import edu.udo.cs.wvtool.config.WVTConfigurationFact;
import edu.udo.cs.wvtool.generic.loader.SourceAsTextLoader;
import edu.udo.cs.wvtool.generic.output.WVTOutputFilter;
import edu.udo.cs.wvtool.generic.stemmer.PorterStemmerWrapper;
import edu.udo.cs.wvtool.generic.vectorcreation.WVTVectorCreator;
import edu.udo.cs.wvtool.main.WVTDocumentInfo;
import edu.udo.cs.wvtool.main.WVTFileInputList;
import edu.udo.cs.wvtool.main.WVTWordVector;
import edu.udo.cs.wvtool.main.WVTool;
import edu.udo.cs.wvtool.util.WVToolException;
import edu.udo.cs.wvtool.wordlist.WVTWordList;

class WordVectorStorage implements WVTOutputFilter {
	
	private Map<String, Map<Integer, Double>> wordVector = new HashMap<String, Map<Integer, Double>>();
	
	public Map<String, Map<Integer, Double>> getWordVector() {
		return wordVector;
	}
	
	@Override
	public void write(WVTWordVector wv) throws WVToolException {
		ExtendedWVTDocumentInfo doc = (ExtendedWVTDocumentInfo) wv.getDocumentInfo();
		double[] vals = wv.getValues();
		Map<Integer, Double> tmp = new HashMap<Integer, Double>();
		for (int i = 0; i < vals.length; i++) {
			if (((Double)vals[i]).equals((Double)0.0))
				continue;
			tmp.put(i, vals[i]);
		}
		wordVector.put(doc.getPullRequestNumber(), tmp);
	}
}

class ExtendedWVTDocumentInfo extends WVTDocumentInfo {

	public String pullRequestNumber; 
	
	public String getPullRequestNumber() {
		return pullRequestNumber;
	}

	public ExtendedWVTDocumentInfo(String sourceName, String contentType,
			String contentEncoding, String contentLanguage, String prNumber) {
		super(sourceName, contentType, contentEncoding, contentLanguage);
		this.pullRequestNumber = prNumber;
	}
	
    public ExtendedWVTDocumentInfo(String sourceName, String contentType, 
    		String contentEncoding, String contentLanguage, int classValue) {
    	super(sourceName, contentType, contentEncoding, contentLanguage, classValue);
    }

	
}

public class WVToolProcess {

	private Map<String, String> prText = new HashMap<String, String>();
	private Map<String, String> issueText = new HashMap<String, String>();
	
	private Map<String, Map<Integer, Double>> wordVector;
	private List<String> words = new ArrayList<String>();
	
	private WVTVectorCreator vectorCreator;
	
	public WVToolProcess(WVTVectorCreator creator) {
		this.vectorCreator = creator;
	}
	
	public Map<String, Map<Integer, Double>> getWordVector() {
		return wordVector;
	}

	public List<String> getWords() {
		return words;
	}

	public void process(String dataSource, int index, DataService ds) throws WVToolException, IOException {
		if (dataSource.equals("pr"))
			extractDescriptionFromPR(index, ds);
		if (dataSource.equals("issue"))
			extractDescriptionFromIssue(index, ds);
		tokenizeAndStemReviewText(dataSource);
	}
	
	public void extractDescriptionFromIssue(int index, DataService ds) {
		for (Map.Entry<String, Issue> each : ds.getDp().getIssues().entrySet()) {
			String issueId = each.getKey();
			String val = each.getValue().getTitle() + " " + each.getValue().getBody();
			issueText.put(issueId, val);
		}
	}
	
	public void extractDescriptionFromPR(int index, DataService ds) {
		for (int i = 0; i < index; i++) {
			String key = ds.getPrList().get(i).getNumber()+"";
			String val = ds.getPrList().get(i).getTitle() + " " + ds.getPrList().get(i).getBody();
			
			prText.put(key, val);
		}
	}
	
	public void tokenizeAndStemReviewText(String dataSource) throws WVToolException, IOException {
		

		WVTool wvt = new WVTool(false);
		WVTConfiguration config = new WVTConfiguration();
		
		config.setConfigurationRule(WVTConfiguration.STEP_LOADER, 
				new WVTConfigurationFact(new SourceAsTextLoader()));
		
		config.setConfigurationRule(WVTConfiguration.STEP_STEMMER, 
				new WVTConfigurationFact(new PorterStemmerWrapper()));
		
		WVTFileInputList list = null;
		if (dataSource.equals("pr")) {
			list = new WVTFileInputList(prText.size());
			
			for (Map.Entry<String, String> each : prText.entrySet()) {
				list.addEntry(new ExtendedWVTDocumentInfo(
						each.getValue(), "", "utf-8", "english", each.getKey()));
			}
		}
		else {
			list = new WVTFileInputList(issueText.size());
			
			for (Map.Entry<String, String> each : issueText.entrySet()) {
				list.addEntry(new ExtendedWVTDocumentInfo(
						each.getValue(), "", "utf-8", "english", each.getKey()));
			}
		}
		
		WVTWordList wordList = wvt.createWordList(list, config);
		
		/* This can be tuned to promote accuracy
		 * 
		 */
		wordList.pruneByFrequency(1, Integer.MAX_VALUE);

		WordVectorStorage wvs = new WordVectorStorage();
		
        config.setConfigurationRule(WVTConfiguration.STEP_OUTPUT, new WVTConfigurationFact(wvs));
        config.setConfigurationRule(WVTConfiguration.STEP_VECTOR_CREATION, 
        		new WVTConfigurationFact(vectorCreator));
        wvt.createVectors(list, config, wordList);
        wordVector = wvs.getWordVector();
        
        for (int i = 0; i < wordList.getNumWords(); i++) {
        	words.add(wordList.getWord(i));
        }
	}
	
	public static void main(String[] args) throws WVToolException, IOException {
	}
}
