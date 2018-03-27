package ReviewerRecommendation.DataProcess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.CommitComment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

public class CommitCommentDataProcess implements IDataParser {

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(String str) {
		
		JSONObject ob = JSONObject.parseObject(str, Feature.IgnoreNotMatch);
		Map<String, List<CommitComment>> commitComments = 
				new HashMap<String, List<CommitComment>>();  
		
		for (Map.Entry<String, Object> each: ob.entrySet()) {
			String key = each.getKey();
			Object val = each.getValue();
			JSONArray jsonArray = ((JSONArray)val);
			commitComments.put(key, (List<CommitComment>)transform(jsonArray));
			
//			for (Map.Entry<String, Object> comment: jsonObject.entrySet()) {
//				String id = comment.getKey();
//				Object vo = comment.getValue();
//				JSONObject jsonOb = ((JSONObject)vo);
//				jsonOb.remove("_id");
//				if (!commitComments.containsKey(key)) {
//					Map<String, CommitComment> innerMap = new HashMap<String, CommitComment>();
//					innerMap.put(id, (CommitComment)transform(jsonOb));
//					commitComments.put(key, innerMap);
//				}
//				else {
//					Map<String, CommitComment> innerMap = commitComments.get(key);
//					innerMap.put(id, (CommitComment)transform(jsonOb));
//				}
//			}
		}
		return commitComments;
	}

	@Override
	public Object transform(Object o) {
		JSONArray ob = (JSONArray)o;
		return ob.toJavaList(CommitComment.class);
//		return JSON.parseObject(str, CommitComment.class, 
//				new ExtraProcessor() {
//					public void processExtra(Object object, String key, Object value) {}
//				}, Feature.IgnoreNotMatch);
	}
}
