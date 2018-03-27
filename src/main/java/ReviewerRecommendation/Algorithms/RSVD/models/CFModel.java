package ReviewerRecommendation.Algorithms.RSVD.models;

import ReviewerRecommendation.Algorithms.RSVD.RatingMatrix;

public interface CFModel {
	public void initial(RatingMatrix mat);
	public double predictVal(int u, int i);
	public void updatePara(int u, int i, double trueVal, double step, RatingMatrix mat);
	public double calculateCostFunction(RatingMatrix mat);
	public void setK(int K);
	public void setLamda(double lamda);
}
