package ReviewerRecommendation.Algorithms.RSVD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/*
 * unknown values are represented by -1
 * 
 */
import java.util.Map;
public class RatingMatrix {
	private List<String> Xaxis;
	private List<String> Yaxis;
//	private Double[][] matrix;
	private Map<Integer, Map<Integer, Double>> matrix = new HashMap<Integer, Map<Integer, Double>>();
	
	private double maxRate = 0.0;
	private double minRate = Double.MAX_VALUE;
	
	public double getMaxRate() {
		return maxRate;
	}

	public double getMinRate() {
		return minRate;
	}
	
	public List<String> getXaxis() {
		return Xaxis;
	}

	public List<String> getYaxis() {
		return Yaxis;
	}
	
	public RatingMatrix(List<String> X, List<String> Y) {
		this.Xaxis = new ArrayList<String>(X);
		this.Yaxis = new ArrayList<String>(Y);
	}
	
	public class Iter {
		private int i = -1, j = 0;
		private List<Map.Entry<Integer, Map<Integer, Double>>> dimen1;
		private List<Map.Entry<Integer, Double>> dimen2;
		
		Iter() {
			dimen1 = new ArrayList
					<Map.Entry<Integer, Map<Integer, Double>>>(matrix.entrySet());
			dimen2 = new ArrayList<Map.Entry<Integer, Double>>
								(dimen1.get(0).getValue().entrySet());
		}
		
		public Double next() {
			while (true) {
				Double ret = updateCoordinate();
				if (ret.equals(Double.MIN_VALUE))
					return ret;
				return dimen2.get(j).getValue();
			}
		}
		
		public int getU() {
			return dimen1.get(i).getKey();
		}
		public int getI() {
			return dimen2.get(j).getKey();
		}
		private Double updateCoordinate() {
			
			if (i == -1) {
				i = 0;
				return 0.0;
			}
			if (j + 1 < dimen2.size()) {
				j ++;
			}
			else if (i + 1 < dimen1.size()) {
				i++;
				dimen2 = new ArrayList<Map.Entry<Integer, Double>>
						(dimen1.get(i).getValue().entrySet());
				j = 0;
			}
			else
				return Double.MIN_VALUE;
			return 0.0;
		}
	}
	
	public Iter iterateMatrix() {
		return new Iter();
	}
	
	public int getX() {
		return Xaxis.size();
	}
	
	public int getY() {
		return Yaxis.size();
	}
	
	public boolean isExist(int i, int j) {
		if (!matrix.containsKey(i) || !matrix.get(i).containsKey(j))
			return false;
		return true;
	}
	
	public Double getVal(int i, int j) {
		if (!matrix.containsKey(i) || !matrix.get(i).containsKey(j))
			return 0.0;
		return matrix.get(i).get(j);
	}
	
	public void setVal(int i, int j, double val) {
		if (!matrix.containsKey(i)) {
			Map<Integer, Double> inner = new HashMap<Integer, Double>();
			inner.put(j, val);
			matrix.put(i, inner);
		}
		else {
			Map<Integer, Double> inner = matrix.get(i);
			inner.put(j, val);
		}
		
		if (val < minRate)	minRate = val;
		if (val > maxRate)	maxRate = val;
	}
	
	public static void main(String[] args) {
		List<String> X = new ArrayList<String>();
		List<String> Y = new ArrayList<String>();
		X.add("1"); X.add("2"); X.add("3");
		Y.add("1"); Y.add("2"); Y.add("3");
		RatingMatrix mat = new RatingMatrix(X, Y);
		mat.setVal(2, 2, 9.0);
		mat.setVal(0, 2, 4.0);
		mat.setVal(1, 1, 3.0);
		mat.setVal(1, 2, 11.0);
		
		System.out.println(mat.getVal(0, 1));
		System.out.println(mat.getVal(0, 2));
		Iter it = mat.iterateMatrix();
		Double example;
		while (!(example = it.next()).equals(Double.MIN_VALUE)) {
			System.out.println(example);
			System.out.println(it.getU() + " " + it.getI());
		}
	}
 }
