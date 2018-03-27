package ReviewerRecommendation.Algorithms.RSVD.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import ReviewerRecommendation.Algorithms.RSVD.RatingMatrix;
import org.eclipse.egit.github.core.CommitComment;

import ReviewerRecommendation.ReReEntrance;
import ReviewerRecommendation.Algorithms.RSVD.RatingMatrix.Iter;

public class NeighborhoodModel implements CFModel{

	private double averRate;
	private int U;
	private int I;
	private Double[] bu;
	private Double[] bi;
	private Map<Integer, Map<Integer, Double>> wij = new HashMap<Integer, Map<Integer, Double>>();
	private Map<Integer, Map<Integer, Double>> cij = new HashMap<Integer, Map<Integer, Double>>();
	private double lamda;
	private int K = 20;
	private ReReEntrance ent;
	private Map<Integer, List<Integer>> similarFiles = new HashMap<Integer, List<Integer>>();
	private RatingMatrix mat;
	private Map<Integer, List<Integer>> userToFilesImplicit = new HashMap<Integer, List<Integer>>();
	private Map<Integer, List<Integer>> userToFilesExplicit = new HashMap<Integer, List<Integer>>();
	
	public NeighborhoodModel(ReReEntrance ent) {
		this.ent = ent;
	}
	
	public void setK(int K) {
		this.K = K;
	}
	
	public void setLamda(double lamda) {
		this.lamda = lamda;
	}
	
	public double getAverRate() { return averRate; }
	
	private List<Integer> relatedFiles(int u, int i) {
		List<Integer> set1 = similarFiles.get(i);
		List<Integer> set2 = userToFilesExplicit.get(u);
		
		List<Integer> ret = new ArrayList<Integer>();
		for (Integer each : set2) {
			if (set1.contains(each))
				ret.add(each);
		}
		return ret;
	}
	
	private List<Integer> implicitRelatedFiles(int u, int i) {
		List<Integer> set1 = similarFiles.get(i);
		List<Integer> set2 = new ArrayList<Integer>();
		if (!userToFilesImplicit.containsKey(u))
			return set2;
		set2 = userToFilesImplicit.get(u);
		
		List<Integer> ret = new ArrayList<Integer>();
		for (Integer each : set2) 
			if (set1.contains(each))
				ret.add(each);
		
		return ret;
	}

	
	@Override
	public void initial(RatingMatrix mat) {
		this.mat = mat;

		// initialize R(i,u)  similarFiles
		List<String> users = mat.getXaxis();
		List<String> files = mat.getYaxis();
		
		initialSimilarFiles(files, users);
		
		System.out.println("similarFiles: " + similarFiles.size());
		
		// initialize R(i,u) Explicit
		for (int i = 0; i < mat.getX(); i++) {
			userToFilesExplicit.put(i, new ArrayList<Integer>());
			for (int j = 0; j < mat.getY(); j++)
				if (mat.isExist(i, j))
					userToFilesExplicit.get(i).add(j);
		}
		
		System.out.println("userToFilesExplicit: " + userToFilesExplicit.size());
		
		// initialize N(i,u) implicit
		Map<String, List<CommitComment>> cc = ent.getDataService().getDp().getCommitComments();
		
		for (Map.Entry<String, List<CommitComment>> each : cc.entrySet()) {
			List<CommitComment> comments = each.getValue();
			for (CommitComment com : comments) {
				String user = "";
				String file = "";
				if (com.getUser() != null)
					user += com.getUser().getLogin();
				if (com.getPath() != null)
					file += com.getPath();
				if (users.contains(user) && files.contains(file)) {
					int U = users.indexOf(user);
					int I = files.indexOf(file);
					if (userToFilesImplicit.containsKey(U) )
						userToFilesImplicit.get(U).add(I);
					else {
						List<Integer> tmp = new ArrayList<Integer>();
						tmp.add(I);
						userToFilesImplicit.put(U, tmp);
					}
				}
			}
		}
		
		System.out.println("userToFilesImplicit: " + userToFilesExplicit.size());
		
		this.U = mat.getX();
		this.I = mat.getY();
		
		bu = new Double[U];
		bi = new Double[I];
		
		for (int i = 0; i < U; i++) 
			bu[i] = Math.random() / 10;
		
		for (int i = 0; i < I; i++)
			bi[i] = Math.random() / 10;
		
		
		double total = 0.0;
		int cnt = 0;
		Iter it = mat.iterateMatrix();
		Double sample;
		while (!(sample = it.next()).equals(Double.MIN_VALUE)) {
			total += sample;
			cnt ++;
		}
		averRate = total / cnt;
		
		System.out.println("averRate: " + averRate);
	}

	public static class Tuple {
		public int fileId;
		public double simScore;
		public Tuple(int fileId, double simScore) 
		{this.fileId = fileId; this.simScore = simScore;}
	}
	
	private void initialSimilarFiles(List<String> files, List<String> users) {

		for (int i = 0; i < files.size(); i++) {
			PriorityQueue<Tuple> minHeap = new PriorityQueue<Tuple>(K*2+1, 
					new Comparator<Tuple>() {
						@Override
						public int compare(Tuple o1, Tuple o2) {
							return Double.compare(o1.simScore, o2.simScore);
						}
			});
			
			for (int j = 0; j < files.size(); j++) {
				double xTwice  = 0.0, yTwice = 0.0, xTotal = 0.0, yTotal = 0.0, xy = 0.0;
				int nij = 0;
				for (int k = 0; k < users.size(); k++) {
					if (mat.isExist(k, i) && mat.isExist(k, j)) {
						nij ++;
						double x = mat.getVal(k, i);
						double y = mat.getVal(k, j);
						xTwice += x*x;
						yTwice += y*y;
						xTotal += x;
						yTotal += y;
						xy += x * y;
					}
				}
				double pearson = 0.0;
				if (nij != 0) {
					pearson = (xy - xTotal*yTotal/nij)/
							Math.sqrt((xTwice - xTotal*xTotal/nij)*(yTwice - yTotal*yTotal/nij));
					pearson =((nij+0.0)/(nij+100.0))*pearson;
				}
				if (pearson != 0.0) {
					if (minHeap.size() < K || pearson > minHeap.peek().simScore) {
						minHeap.offer(new Tuple(j, pearson));
						if (minHeap.size() > K) 
							minHeap.poll();
					}
				}
			}
			
			similarFiles.put(i, new ArrayList<Integer>());
			for (Tuple t : minHeap) {
				similarFiles.get(i).add(t.fileId);
			}
		}
	}
	
	private Double getWij(int i, int j) {
		if (!wij.containsKey(i)) {
			Map<Integer, Double> inner = new HashMap<Integer, Double>();
			inner.put(j, Math.random() / 10);
			wij.put(i, inner);
			return wij.get(i).get(j);
		}
		else {
			Map<Integer, Double> inner = wij.get(i);
			if (!inner.containsKey(j)) {
				inner.put(j, Math.random() / 10);
				return wij.get(i).get(j);
			}
			else
				return wij.get(i).get(j);
		}
	}
	
	private void setWij(int i, int j, double val) {
		wij.get(i).put(j, val);
	}
	
	private void setCij(int i, int j, double val) {
		cij.get(i).put(j, val);
	}
	
	private Double getCij(int i, int j) {
		if (!cij.containsKey(i)) {
			Map<Integer, Double> inner = new HashMap<Integer, Double>();
			inner.put(j, Math.random() / 10);
			cij.put(i, inner);
			return cij.get(i).get(j);
		}
		else {
			Map<Integer, Double> inner = cij.get(i);
			if (!inner.containsKey(j)) {
				inner.put(j, Math.random() / 10);
				return cij.get(i).get(j);
			}
			else
				return cij.get(i).get(j);
		}
	}
	
	@Override
	public double predictVal(int u, int i) {
		double eval = averRate + bu[u] + bi[i];
		
		List<Integer> Rk = relatedFiles(u, i);
		List<Integer> Nk = implicitRelatedFiles(u, i);
		
		double Rconst;
		if (Rk.size() == 0)	Rconst = 0.0;
		else	Rconst = 1.0 / Math.sqrt(Rk.size()+0.0);
		
		double Nconst;
		if (Nk.size() == 0) Nconst = 0.0;
		else	Nconst = 1.0 / Math.sqrt(Nk.size()+0.0);
		
		double R = 0.0;
		for (Integer j : Rk) {
			double ruj = mat.getVal(u, j);
			double buj = averRate + bu[u] + bi[j];	
			R += (ruj - buj) * getWij(i,j);
		}
		
		eval += Rconst * R;
		
		double N = 0.0;
		for (Integer j : Nk) {
			N += getCij(i,j);
		}
		eval += Nconst * N;
		return eval;
	}
	
	private double error(int u, int i, double trueVal, RatingMatrix mat) {
		double eval = predictVal(u, i);
		
		if (eval < mat.getMinRate()) eval = mat.getMinRate();
		if (eval > mat.getMaxRate()) eval = mat.getMaxRate();
		
		return trueVal - eval;
	}
	
	@Override
	public void updatePara(int u, int i, double trueVal, double step,
			RatingMatrix mat) {
		double err = error(u, i, trueVal, mat);
		bu[u] = bu[u] + step * (err - lamda * bu[u]);
		bi[i] = bi[i] + step * (err - lamda * bi[i]);
		
		List<Integer> Rk = relatedFiles(u, i);
		double Rconst = 0.0;
		if (Rk.size() != 0) Rconst = 1.0 / Math.sqrt(Rk.size()+0.0);
		
		for (Integer j : Rk) {
			double ruj = mat.getVal(u, j);
			double buj = averRate + bu[u] + bi[j];
			setWij(i, j, getWij(i,j) + step*(Rconst * err *(ruj - buj) - lamda * getWij(i,j)));
		}
		
		List<Integer> Nk = implicitRelatedFiles(u, i);
		double Nconst = 0.0;
		if (Nk.size() != 0) Nconst = 1.0 / Math.sqrt(Nk.size()+0.0);
		
		for (Integer j : Nk) {
			setCij(i, j, getCij(i,j) + step * (Nconst * err - lamda * getCij(i,j)));
		}
	}

	@Override
	public double calculateCostFunction(RatingMatrix mat) {
		double cost = 0.0;
		Iter it = mat.iterateMatrix();
		Double example;
		while (!(example = it.next()).equals(Double.MIN_VALUE)) {
			int u = it.getU();
			int i = it.getI();
			cost += (example - predictVal(u, i)) * (example - predictVal(u, i));
		}
		
		double r = 0.0;
		for (Map.Entry<Integer, Map<Integer, Double>> each : wij.entrySet()) {
			for (Map.Entry<Integer, Double> inner : each.getValue().entrySet()) {
				r += inner.getValue() * inner.getValue();
			}
		}
		
		double n = 0.0;
		for (Map.Entry<Integer, Map<Integer, Double>> each : cij.entrySet()) {
			for (Map.Entry<Integer, Double> inner : each.getValue().entrySet()) {
				n += inner.getValue() * inner.getValue();
			}
		}
		
		double user = 0.0;
		for (int i = 0; i < mat.getX(); i++)
			user += bu[i] * bu[i];
		
		double item = 0.0;
		for (int i = 0; i < mat.getY(); i++)
			item += bi[i] * bi[i];
		
		return cost + lamda * (r + n + user + item);
	}

}
