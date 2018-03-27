package ReviewerRecommendation.DataProcess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.CommitFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

public class PRFilesDataProcess implements IDataParser {

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(String str) {
		
		JSONObject ob = JSONObject.parseObject(str, Feature.IgnoreNotMatch);
		Map<String, List<CommitFile>> prFiles = new HashMap<String, List<CommitFile>>();  
		
		for (Map.Entry<String, Object> each: ob.entrySet()) {
			String key = each.getKey();
			Object val = each.getValue();
			JSONArray jsonArray = ((JSONArray)val);
			prFiles.put(key, (List<CommitFile>)transform(jsonArray));
			
//			for (Map.Entry<String, Object> comment: jsonObject.entrySet()) {
//				String id = comment.getKey();
//				Object vo = comment.getValue();
//				JSONObject jsonOb = ((JSONObject)vo);
//				jsonOb.remove("_id");
//				if (!prComments.containsKey(key)) {
//					Map<String, PullRequestComment> innerMap = new HashMap<String, PullRequestComment>();
//					innerMap.put(id, (PullRequestComment)transform(jsonOb));
//					prComments.put(key, innerMap);
//				}
//				else {
//					Map<String, PullRequestComment> innerMap = prComments.get(key);
//					innerMap.put(id, (PullRequestComment)transform(jsonOb));
//				}
//			}
		}
		return prFiles;
	}

	@Override
	public Object transform(Object o) {
		JSONArray ob = (JSONArray)o;
		return ob.toJavaList(CommitFile.class);
//		PullRequestComment prComment = JSON.parseObject(str, CommitComment.class, 
//				new ExtraProcessor() {
//					public void processExtra(Object object, String key, Object value) {}
//				}, Feature.IgnoreNotMatch);
//		return prComment;
	}

}
