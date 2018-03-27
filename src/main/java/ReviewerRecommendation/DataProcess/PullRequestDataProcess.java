package ReviewerRecommendation.DataProcess;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.PullRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;

public class PullRequestDataProcess implements IDataParser{

	@Override
	public Object parse(String str) {
		
		JSONObject ob = JSONObject.parseObject(str, Feature.IgnoreNotMatch);
		
		Map<String, PullRequest> prs = new HashMap<String, PullRequest>();  
		for (Map.Entry<String, Object> each: ob.entrySet()) {
			String key = each.getKey();
			Object val = each.getValue();
			JSONObject jsonObject = ((JSONObject)val);
//			jsonObject.remove("_id");
			prs.put(key, (PullRequest)transform(jsonObject));
		}
		return prs;
	}

	@Override
	public Object transform(Object o) {
		JSONObject ob =(JSONObject)o;
		String str = ob.toJSONString();
		PullRequest pr = JSON.parseObject(str, PullRequest.class, 
				new ExtraProcessor() {
					public void processExtra(Object object, String key, Object value) {}
				}, Feature.IgnoreNotMatch);
		return pr;
	}
}
