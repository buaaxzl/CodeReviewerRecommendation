package ReviewerRecommendation.DataProcess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.RepositoryCommit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

public class PRCommitsDataProcess implements IDataParser {

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(String str) {
		
		JSONObject ob = JSONObject.parseObject(str, Feature.IgnoreNotMatch);
		Map<String, List<RepositoryCommit>> prCommits = new HashMap<String, List<RepositoryCommit>>();  
		
		for (Map.Entry<String, Object> each: ob.entrySet()) {
			String key = each.getKey();
			Object val = each.getValue();
			JSONArray jsonArray = ((JSONArray)val);
			prCommits.put(key, (List<RepositoryCommit>)transform(jsonArray));
		}
		return prCommits;
	}

	@Override
	public Object transform(Object o) {
		JSONArray ob = (JSONArray)o;
		return ob.toJavaList(RepositoryCommit.class);
	}
}
