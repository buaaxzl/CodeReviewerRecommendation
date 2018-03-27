package ReviewerRecommendation.DataProcess;

import org.eclipse.egit.github.core.Repository;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

public class RepoDataProcess implements IDataParser{

	@Override
	public Object parse(String str) {
		JSONObject ob = JSONObject.parseObject(str, Feature.IgnoreNotMatch);
//		ob.remove("_id");
		return ob.toJavaObject(Repository.class);
	}

	@Override
	public Object transform(Object ob) {
		return null;
	}
}
