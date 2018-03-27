package ReviewerRecommendation.Algorithms.RSVD.train;

import ReviewerRecommendation.Algorithms.RSVD.models.CFModel;
import ReviewerRecommendation.Algorithms.RSVD.RatingMatrix;
import ReviewerRecommendation.Algorithms.RSVD.RatingMatrix.Iter;

public class Trainer {
	private CFModel model;
	private double step;
	private int times = 100;
	private RatingMatrix mat;
	private double ratio = 0.001;
	
	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public void setMat(RatingMatrix mat) {
		this.mat = mat;
	}

	public void setTimes(int times) {
		this.times = times;
	}
	
	public void setModel(CFModel model) {
		this.model = model;
	}
	
	public void setStep(double step) {
		this.step = step;
	}
	
	public Trainer(RatingMatrix mat, CFModel model) {
		this.mat = mat;
		this.model = model;
	}
	
	public void train() {
		model.initial(mat);
		int t = 0;
		double lastCost = 0.0;
		
		int th = 0;
		while (t < times) {
			Iter it = mat.iterateMatrix();
			Double example;
			while (!(example = it.next()).equals(Double.MIN_VALUE)) {
				int u = it.getU();
				int i = it.getI();
				model.updatePara(u, i, example, step, mat);
			}
			t++;
			
			double cost = model.calculateCostFunction(mat);
//			System.out.println("time: " + t + " cost: " + cost);
		
			if (lastCost != 0.0 && Math.abs(lastCost - cost) / lastCost < ratio) 
				th++;
			
			if (th > 10) break;
			
			lastCost = cost;
//			step *= 0.99;
		}
	}
}
