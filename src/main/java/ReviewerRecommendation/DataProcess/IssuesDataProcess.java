package ReviewerRecommendation.DataProcess;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.Issue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;

public class IssuesDataProcess implements IDataParser{

	@Override
	public Object parse(String str) {
		
		JSONObject ob = JSON.parseObject(str, Feature.IgnoreNotMatch);
		
		Map<String, Issue> issues = new HashMap<String, Issue>();  
		for (Map.Entry<String, Object> each: ob.entrySet()) {
			String key = each.getKey();
			Object val = each.getValue();
			JSONObject jsonObject = ((JSONObject)val);
//			jsonObject.remove("_id");
			issues.put(key, (Issue)transform(jsonObject));
		}
		return issues;
	}

	@Override
	public Object transform(Object o) {
		JSONObject ob = (JSONObject)o;
		String str = ob.toJSONString();
		return JSON.parseObject(str, Issue.class, 
				new ExtraProcessor() {
					public void processExtra(Object object, String key, Object value) {}
				}, Feature.IgnoreNotMatch);
	}
	
	
}
