package ReviewerRecommendation.Algorithms.RSVD.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import ReviewerRecommendation.Algorithms.RSVD.RatingMatrix;

import ReviewerRecommendation.ReReEntrance;
import ReviewerRecommendation.Algorithms.RSVD.RatingMatrix.Iter;

public class BlenderWithOutImplicitFeedBack implements CFModel{

	private int U;
	private int I;
	private Map<Integer, Map<Integer, Double>> wij = new HashMap<Integer, Map<Integer, Double>>();

	public Double[][] puk;
	public Double[][] qki;
	
	private double lamda;
	private int K = 30;
	private int factors = 20;
	private ReReEntrance ent;
	private Map<Integer, List<Integer>> similarItems = new HashMap<Integer, List<Integer>>();
	private RatingMatrix mat;
	private Map<Integer, List<Integer>> userToItemsExplicit = new HashMap<Integer, List<Integer>>();
	
	public BlenderWithOutImplicitFeedBack(ReReEntrance ent) {
		this.ent = ent;
	}
	
	public void setK(int K) {
		this.K = K;
	}
	
	public void setLamda(double lamda) {
		this.lamda = lamda;
	}
	
	private List<Integer> relatedItems(int u, int i) {
		List<Integer> set1 = similarItems.get(i);
		List<Integer> set2 = userToItemsExplicit.get(u);
		
		List<Integer> ret = new ArrayList<Integer>();
		for (Integer each : set2) {
			if (set1.contains(each))
				ret.add(each);
		}
		return ret;
	}
	

	@Override
	public void initial(RatingMatrix mat) {
		this.mat = mat;

		// initialize R(i,u)  similarFiles
		List<String> users = mat.getXaxis();
		List<String> items = mat.getYaxis();
		
		initialSimilarItems(items, users);
		
		System.out.println("similarItems: " + similarItems.size());
		
		// initialize R(i,u) Explicit
		for (int i = 0; i < mat.getX(); i++) {
			userToItemsExplicit.put(i, new ArrayList<Integer>());
			for (int j = 0; j < mat.getY(); j++)
				if (mat.isExist(i, j))
					userToItemsExplicit.get(i).add(j);
		}
		
		System.out.println("userToItemsExplicit: " + userToItemsExplicit.size());
		
		this.U = mat.getX();
		this.I = mat.getY();
		
		puk = new Double[U][factors];
		qki = new Double[factors][I];
		
		for (int i = 0; i < U; i++) 
			for (int j = 0; j < factors; j++) {
				puk[i][j] = Math.random() / 10;
			}
		
		for (int i = 0; i < factors; i++) 
			for (int j = 0; j < I; j++) {
				qki[i][j] = Math.random() / 10;
			}
	}
	
	class Tuple {
		public int itemId;
		public double simScore;
		public Tuple(int itemId, double simScore) 
		{this.itemId = itemId; this.simScore = simScore;}
	}
	
	private void initialSimilarItems(List<String> items, List<String> users) {
		for (int i = 0; i < items.size(); i++) {
			PriorityQueue<Tuple> minHeap = new PriorityQueue<Tuple>(K*2+1, 
					new Comparator<Tuple>() {
						@Override
						public int compare(Tuple o1, Tuple o2) {
							return Double.compare(o1.simScore, o2.simScore);
						}
			});
			
			for (int j = 0; j < items.size(); j++) {
				
				if (i == j) continue;
				
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
			
			similarItems.put(i, new ArrayList<Integer>());
			for (Tuple t : minHeap) {
				similarItems.get(i).add(t.itemId);
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
	
	private double innerProduct(int u, int i) {
		double ret = 0.0;
		for (int j = 0; j < factors; j++) {
			ret += puk[u][j] * qki[j][i];
		}
		return ret;
	}
	
	@Override
	public double predictVal(int u, int i) {
		double eval = innerProduct(u, i);
		
		List<Integer> Rk = relatedItems(u, i);

		double Rconst;
		if (Rk.size() == 0)	Rconst = 0.0;
		else	Rconst = 1.0 / Math.sqrt(Rk.size()+0.0);
		
		double R = 0.0;
		for (Integer j : Rk) {
			double ruj = mat.getVal(u, j);
			double buj = innerProduct(u, j);	
			R += (ruj - buj) * getWij(i,j);
		}
		
		eval += Rconst * R;
		
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

		for (int k = 0; k < factors; k++) {
			double oldpuk = puk[u][k];
			
			puk[u][k] = puk[u][k] + 
					step*(err*qki[k][i] - lamda * puk[u][k]);
			qki[k][i] = qki[k][i] + 
					step*(err*oldpuk - lamda * qki[k][i]);
		}
		
		List<Integer> Rk = relatedItems(u, i);
		double Rconst = 0.0;
		if (Rk.size() != 0) Rconst = 1.0 / Math.sqrt(Rk.size()+0.0);
		
		for (Integer j : Rk) {
			double ruj = mat.getVal(u, j);
			double buj = innerProduct(u, j);
			setWij(i, j, getWij(i,j) + step*(Rconst * err *(ruj - buj) - lamda * getWij(i,j)));
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

		double p = 0.0;
		for (int i = 0; i < mat.getX(); i++)
			for (int k = 0; k < factors; k ++)
				p += puk[i][k] * puk[i][k];
		
		double q = 0.0;
		for (int i = 0; i < mat.getY(); i++)
			for (int k = 0; k < factors; k ++)
				q += qki[k][i] * qki[k][i];
		
		return cost + lamda * (r + p + q);
	}
}
