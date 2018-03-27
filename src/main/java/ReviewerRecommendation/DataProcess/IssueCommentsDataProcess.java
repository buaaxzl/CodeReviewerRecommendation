package ReviewerRecommendation.DataProcess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Comment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

public class IssueCommentsDataProcess implements IDataParser{

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(String str) {
		
		JSONObject ob = JSONObject.parseObject(str, Feature.IgnoreNotMatch);
		Map<String, List<Comment>> issueComments = 
				new HashMap<String, List<Comment>>();  
		
		for (Map.Entry<String, Object> each: ob.entrySet()) {
			String key = each.getKey();
			Object val = each.getValue();
			JSONArray jsonArray = ((JSONArray)val);
			issueComments.put(key, (List<Comment>)transform(jsonArray));
			
//			for (Map.Entry<String, Object> comment: jsonObject.entrySet()) {
//				String id = comment.getKey();
//				Object vo = comment.getValue();
//				JSONObject jsonOb = ((JSONObject)vo);
//				jsonOb.remove("_id");
//				if (!issueComments.containsKey(key)) {
//					Map<String, IssueComment> innerMap = new HashMap<String, IssueComment>();
//					innerMap.put(id, (IssueComment)transform(jsonOb));
//					issueComments.put(key, innerMap);
//				}
//				else {
//					Map<String, IssueComment> innerMap = issueComments.get(key);
//					innerMap.put(id, (IssueComment)transform(jsonOb));
//				}
//			}
		}
		return issueComments;
	}

	@Override
	public Object transform(Object o) {
		JSONArray ob = (JSONArray)o;
		return ob.toJavaList(Comment.class);
//		return JSON.parseObject(str, IssueComment.class, 
//				new ExtraProcessor() {
//					public void processExtra(Object object, String key, Object value) {}
//				}, Feature.IgnoreNotMatch);
	}

}
