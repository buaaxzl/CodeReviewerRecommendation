package ReviewerRecommendation.Algorithms.Collaboration;

import ReviewerRecommendation.Algorithms.RecommendAlgo;
import ReviewerRecommendation.DataService.Pair;
import ReviewerRecommendation.ReReEntrance;
import org.eclipse.egit.github.core.PullRequest;
import org.gephi.graph.api.*;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;
import tools.SortMapElement;

import java.util.*;
import java.util.Map.Entry;

public class CommentNetwork implements RecommendAlgo{
	
	class Tuple {
		String name;
		Double val;
		Tuple(String name, Double val) {
			this.name = name;
			this.val = val;
		}
		public String getName() {
			return name;
		}
		public Double getVal() {
			return val;
		}
	}
	
	class MyNode {
		Node node;
		Integer indegree;
		public MyNode(Node node, int in) {
			this.node = node;
			this.indegree = in;
		}
		public Node getNode() {
			return node;
		}
		public Integer getIndegree() {
			return indegree;
		}
	}
	
	private boolean isBuild = false;
	public Map<String, PriorityQueue<Tuple>> commentNet = new HashMap<String, PriorityQueue<Tuple>>();
	
	private Set<String> reviewersSet = new HashSet<String>();
	private Map<Integer, Set<String>> reviewersPerPR = new HashMap<Integer, Set<String>>();
	
	private List<Map.Entry<String, PriorityQueue<MyNode>>> communityList = null;
	
	private Map<String, Double> reviewersScore;
	
	public Map<String, Double> getReviewersScore() {
		return reviewersScore;
	}

	private void trainModel(int cur, ReReEntrance ent) {
		constructNetwork(cur, ent);
		mineFrequentItemsets(cur, ent);
		communityDetection();
	}

	private void communityDetection() {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get controllers and models
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);

        DirectedGraph directedGraph = graphModel.getDirectedGraph();

        for (Map.Entry<String, PriorityQueue<Tuple>> each : commentNet.entrySet()) {
        	String ni = each.getKey();
        	for (Tuple t : each.getValue()) {
        		String nj = t.getName();
        		double val = t.getVal();
        		
        		Node a, b;
        		if (directedGraph.getNode(ni) == null) {
        			a = graphModel.factory().newNode(ni);
        			directedGraph.addNode(a);
        		}
        		else
        			a = directedGraph.getNode(ni);
        		
        		if (directedGraph.getNode(nj) == null) {
        			b = graphModel.factory().newNode(nj);
        			directedGraph.addNode(b);
        		}
        		else
        			b = directedGraph.getNode(nj);
        		Edge edge = graphModel.factory().newEdge(a, b, 0, val, true);
                directedGraph.addEdge(edge);
        	}
        }

        System.out.println("Nodes: " + directedGraph.getNodeCount() + 
        		" Edges: " + directedGraph.getEdgeCount());
        
        //Run modularity algorithm - community detection
        Modularity modularity = new Modularity();
        modularity.setRandom(true);
        modularity.setResolution(1.0);
        modularity.setUseWeight(false);
        modularity.execute(graphModel);
        
        Map<String, PriorityQueue<MyNode>> communities = new HashMap<String, PriorityQueue<MyNode>>();
        
        for (Node n : directedGraph.getNodes()) {
        	String key = ((Integer)n.getAttribute(Modularity.MODULARITY_CLASS)).toString();
        	int in = directedGraph.getInDegree(n);
        	MyNode myNode = new MyNode(n, in);
        	
        	if (communities.containsKey(key))
    			communities.get(key).offer(myNode);
    		else {
    			PriorityQueue<MyNode> tmp = new PriorityQueue<MyNode>(11, new Comparator<MyNode>() {
					@Override
					public int compare(MyNode o1, MyNode o2) {
						return o2.getIndegree() - o1.getIndegree();
					}
				});
    			tmp.offer(myNode);
    			communities.put(key, tmp);
    		}
        }
        
        communityList = 
        		new ArrayList<Map.Entry<String, PriorityQueue<MyNode>>>(communities.entrySet());
        
        Collections.sort(communityList, new Comparator<Map.Entry<String, PriorityQueue<MyNode>>>() {
			@Override
			public int compare(Entry<String, PriorityQueue<MyNode>> o1,
					Entry<String, PriorityQueue<MyNode>> o2) {
				return o2.getValue().size() - o1.getValue().size();
			}
		});
        
        pc.closeCurrentProject();
	}

	private void mineFrequentItemsets(int cur, ReReEntrance ent) {
		for (int i = 0; i < cur; i++) {
			Set<String> tmp = new HashSet<String>();
			
			if (ent.getDataService().getPrList().get(i).getUser() != null) {
				String author = ent.getDataService().getPrList().get(i).getUser().getLogin();
				tmp.add(author);
			}
			List<String> revs = ent.getDataService().getCodeReviewers(i);
			tmp.addAll(revs);
			reviewersPerPR.put(i, tmp);
			reviewersSet.addAll(tmp);
		}
	}

	private void constructNetwork(int cur, ReReEntrance ent) {
		Date startTime = ent.getDataService().getPrList().get(0).getCreatedAt();
		Date endTime = ent.getDataService().getPrList().get(cur).getCreatedAt();
		
		Map<String, Map<String, Map<Integer, List<Pair>>>> record = 
				new HashMap<String, Map<String, Map<Integer, List<Pair>>>>();
		
		for (int i = 0; i < cur; i++) {
			if (ent.getDataService().getPrList().get(i).getUser() == null) continue;
			String author = ent.getDataService().getPrList().get(i).getUser().getLogin();
			
			Map<String, List<Pair>> cont = ent.getDataService().getReviewerContribution(i);
			Map<String, Map<Integer, List<Pair>>> comments = 
					new HashMap<String, Map<Integer, List<Pair>>>();
			
			for (Map.Entry<String, List<Pair>> each : cont.entrySet()) {
				Map<Integer, List<Pair>> tmp = new HashMap<Integer, List<Pair>>();
				tmp.put(i, each.getValue());
				comments.put(each.getKey(), tmp);
			}
			
			if (record.containsKey(author)) {
				Map<String, Map<Integer, List<Pair>>> tmp = record.get(author);
				for (Map.Entry<String, Map<Integer, List<Pair>>> each : comments.entrySet()) {
					if (tmp.containsKey(each.getKey())) {
						if (each.getValue().size() != 1)
							System.out.println("**********************");
						tmp.get(each.getKey()).putAll(each.getValue());
					}
					else
						tmp.put(each.getKey(), each.getValue());
				}
			}
			else {
				record.put(author, comments);
			}
		}
		
		for (Map.Entry<String,  Map<String, Map<Integer, List<Pair>>>> each1 : record.entrySet()) {
			String vi = each1.getKey();
			for (Map.Entry<String, Map<Integer, List<Pair>>> each2 : each1.getValue().entrySet()) {
				String vj = each2.getKey();
				
				double val = 0.0;
				for (Map.Entry<Integer, List<Pair>> each3 : each2.getValue().entrySet()) {
					double lamda = 1.0;
					List<Pair> pairs = each3.getValue();
					Collections.sort(pairs, new Comparator<Pair>() {
						@Override
						public int compare(Pair o1, Pair o2) {
							return o2.getDate().compareTo(o1.getDate());
						}
					});
					
					for (Pair comment : pairs) {
						val += lamda * ((comment.getDate().getTime()+0.0 - startTime.getTime()+0.0)/
											(endTime.getTime()+0.0 - startTime.getTime()+0.0));
						lamda = lamda * 0.8;
					}
				}
				
				if (commentNet.containsKey(vi))
					commentNet.get(vi).add(new Tuple(vj, val));
				else {
					PriorityQueue<Tuple> tmp = new PriorityQueue<Tuple>(11, new Comparator<Tuple>() {
						@Override
						public int compare(Tuple o1, Tuple o2) {
							return o2.getVal().compareTo(o1.getVal());
						}
					});
					tmp.add(new Tuple(vj, val));
					
					commentNet.put(vi, tmp);
				}
			}
		}
	}

	@Override
	public List<String> recommend(int i, ReReEntrance ent) {
		if (!isBuild) {
			isBuild = true;
			trainModel(i, ent);
		}
		
		reviewersScore = new HashMap<String, Double>();
		
		Set<String> ret = new HashSet<String>();
		String user = ent.getDataService().getPrList().get(i).getUser().getLogin();
		if (user == null) return null;
		
		if (commentNet.containsKey(user)) {
			recommendForPAC(ent, ret, user);
		}
		else {
			/*
			 * process PNC Apriori
			 */
			if (reviewersSet.contains(user)) {

				Map<String, Integer> count = new HashMap<String, Integer>();
				for (Map.Entry<Integer, Set<String>> each : reviewersPerPR.entrySet()) {
					if (each.getValue().contains(user)) {
						for (String person : each.getValue()) {
							if (person.equals(user)) continue;
							if (count.containsKey(person))
								count.put(person, count.get(person) + 1);
							else
								count.put(person, 1);
						}
					}
				}

				List<Map.Entry<String, Integer>> res = SortMapElement.sortIntegerDesc(count);
				for (int j = 0; j < ent.getK() && j < res.size(); j++) {
					ret.add(res.get(j).getKey());
					reviewersScore.put(res.get(j).getKey(), res.get(j).getValue().doubleValue());
				}
			}
			/*
			 * process PNC gephi
			 */
			else {

				List<PriorityQueue<MyNode>> copy = new ArrayList<PriorityQueue<MyNode>>();
				for (Map.Entry<String, PriorityQueue<MyNode>> each : communityList) {
					if (each.getValue().size() < 2) continue;
					copy.add(new PriorityQueue<MyNode>(each.getValue()));
				}

				while( ret.size() != ent.getK()) {
					for (PriorityQueue<MyNode> each : copy) {
						if (each.size() == 0) continue;
						MyNode node = each.poll();
						ret.add((String)node.getNode().getId());
						reviewersScore.put((String)node.getNode().getId(),
											node.getIndegree().doubleValue());
						if (ret.size() == ent.getK())
							break;
					}
				}
			}
		}
		return new ArrayList<String>(ret);
	}

	private void recommendForPAC(ReReEntrance ent, Set<String> ret, String user) {
		Queue<String> q = new LinkedList<String>();
		q.offer(user);
		while (!q.isEmpty()) {
			String str = q.poll();
			PriorityQueue<Tuple> inner = commentNet.get(str);
			if (inner == null) continue;
			
			PriorityQueue<Tuple> tmp = new PriorityQueue<Tuple>(inner);
			while (!tmp.isEmpty()) {
				Tuple t = tmp.poll();
				if (ret.contains(t.getName())) continue;
				ret.add(t.getName());
				reviewersScore.put(t.getName(), t.getVal());
				q.offer(t.getName());
				if (ret.size() == ent.getK())
					break;
			}
			if (ret.size() == ent.getK())
				break;
		}
	}
	
	public static void main(String[] args) {
	}
}
