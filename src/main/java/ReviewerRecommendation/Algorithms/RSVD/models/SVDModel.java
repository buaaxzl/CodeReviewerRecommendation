package ReviewerRecommendation.Algorithms.RSVD.models;

import ReviewerRecommendation.Algorithms.RSVD.RatingMatrix;
import ReviewerRecommendation.Algorithms.RSVD.RatingMatrix.Iter;

public class SVDModel implements CFModel{
	
	public int K = 10;
	public int U;
	public int I;
	private double lamda;
	public Double[][] puk;
	public Double[][] qki;
	public Double[] bu;
	public Double[] bi;
	public double averRate;
	
	public void setLamda(double lamda) {
		this.lamda = lamda;
	}
	
	public void setK(int K) {
		this.K = K;
	}
	
	@Override
	public void initial(RatingMatrix mat) {
		this.U = mat.getX();
		this.I = mat.getY();
		
		puk = new Double[U][K];
		qki = new Double[K][I];
		bu = new Double[U];
		bi = new Double[I];
		
		for (int i = 0; i < U; i++) 
			for (int j = 0; j < K; j++)
				puk[i][j] = Math.random() / 10;
		
		for (int i = 0; i < K; i++) 
			for (int j = 0; j < I; j++)
				qki[i][j] = Math.random() / 10;
		
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
	}
	
	public double predictVal(int u, int i) {
		return averRate + bu[u] + bi[i] + innerProduct(u, i);
	}
	
	public void updatePara(int u, int i, double trueVal, double step, RatingMatrix mat) {
		double err = error(u, i, trueVal, mat);

		for (int k = 0; k < K; k++) {
			double oldpuk = puk[u][k];

			puk[u][k] = puk[u][k] + 
					step*(err*qki[k][i] - lamda * puk[u][k]);
			qki[k][i] = qki[k][i] + 
					step*(err*oldpuk - lamda * qki[k][i]);
		}
		bu[u] = bu[u] + step*(err - lamda*bu[u]);
		bi[i] = bi[i] + step*(err - lamda*bi[i]);
	}
	
	private double error(int u, int i, double trueVal, RatingMatrix mat) {

		double eval = averRate + bu[u] + bi[i] + innerProduct(u, i);
		if (eval < mat.getMinRate()) eval = mat.getMinRate();
		if (eval > mat.getMaxRate()) eval = mat.getMaxRate();
		

		return trueVal - eval;
	}
	
	private double innerProduct(int u, int i) {
		double ret = 0.0;
		for (int j = 0; j < K; j++) {
			ret += puk[u][j] * qki[j][i];
		}
		return ret;
	}
	
	public double calculateCostFunction(RatingMatrix mat) {
		double cost = 0.0;
		
		Iter it = mat.iterateMatrix();
		Double example;
		while (!(example = it.next()).equals(Double.MIN_VALUE)) {
			int u = it.getU();
			int i = it.getI();
			cost += (example - predictVal(u, i)) * (example - predictVal(u, i));
		}
		
		double p = 0.0;
		for (int i = 0; i < mat.getX(); i++)
			for (int k = 0; k < K; k ++)
				p += puk[i][k] * puk[i][k];
		
		double q = 0.0;
		for (int i = 0; i < mat.getY(); i++)
			for (int k = 0; k < K; k ++)
				q += qki[k][i] * qki[k][i];
		
		double user = 0.0;
		for (int i = 0; i < mat.getX(); i++)
			user += bu[i] * bu[i];
		
		double item = 0.0;
		for (int i = 0; i < mat.getY(); i++)
			item += bi[i] * bi[i];
		
		return 0.5 * cost + 0.5 * lamda * (p + q + user + item);
	}
}
