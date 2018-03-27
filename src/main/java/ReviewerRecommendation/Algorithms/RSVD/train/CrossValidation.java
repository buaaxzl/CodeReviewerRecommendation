package ReviewerRecommendation.Algorithms.RSVD.train;

import java.util.Random;

import ReviewerRecommendation.Algorithms.RSVD.*;
import ReviewerRecommendation.Algorithms.RSVD.models.*;
import ReviewerRecommendation.ReReEntrance;
import ReviewerRecommendation.Algorithms.RSVD.RatingMatrix.Iter;

public class CrossValidation {

	private RatingMatrix mat;
	private RatingMatrix trainMat;
	private RatingMatrix testMat;
	private ReReEntrance ent;
	
	public CrossValidation(RatingMatrix mat, ReReEntrance ent) {
		this.mat = mat;
		this.ent = ent;
	}
	
	private void splitMat() {
		trainMat = new RatingMatrix(mat.getXaxis(), mat.getYaxis());
		testMat = new RatingMatrix(mat.getXaxis(), mat.getYaxis());
		
		Random ran = new Random(System.currentTimeMillis());
		Iter it = mat.iterateMatrix();
		Double sample;
		
		while (!(sample = it.next()).equals(Double.MIN_VALUE)) {
			int psu = ran.nextInt(10);
			if (psu < 8) {
				trainMat.setVal(it.getU(), it.getI(), sample);
			}
			else {
				testMat.setVal(it.getU(), it.getI(), sample);
			}
		}
	}

	private CFModel newModel(String modelName) {
		if (modelName.equals("SVDModel"))
			return new SVDModel();
		if (modelName.equals("Neigh"))
			return new NeighborhoodModel(ent);
		if (modelName.equals("BasicSVD"))
			return new BasicSVD();
		if (modelName.equals("Blender"))
			return new Blender(ent);
		if (modelName.equals("BlenderW"))
			return new BlenderWithOutImplicitFeedBack(ent);
		return null;
	}
	
	public CFModel validate(String modelName) {
		double minimum = Double.MAX_VALUE;
		double lamdaValue = 0.18, stepValue = 0.005;
		int kValue = 30;
		
//		splitMat();
//		
//		List<Integer> factorsNum = new ArrayList<Integer>(Arrays.asList(10, 20, 50));
//		List<Double> lamdaList = new ArrayList<Double>(Arrays.asList(0.5, 0.1, 0.05));
//		List<Double> stepList = new ArrayList<Double>(Arrays.asList(0.1, 0.01, 0.05, 0.005));
		
		Trainer trainer = new Trainer(trainMat, null);
		
//		for (Integer k : factorsNum) {
//			for (Double lamda : lamdaList) {
//				for (Double step : stepList) {
//					
//					CFModel model = newModel(modelName);
//					model.setLamda(lamda);
//					model.setLamda(k);
//					
//					trainer.setStep(step);
//					trainer.setModel(model);
//					trainer.setMat(trainMat);
//					trainer.train();
//					
//					double erm = evaluate(model);
//					if (erm < minimum) {
//						
//						minimum = erm;
//						lamdaValue = lamda;
//						stepValue = step;
//						kValue = k;
//						
//						System.out.println(k + " " + lamda + " " + step);
//						System.out.println(erm);
//					}
//				}
//			}
//		}
		
		//reTrain model with all data
		trainer.setStep(stepValue);
		
		CFModel model = newModel(modelName);
		model.setK(kValue);
		model.setLamda(lamdaValue);
		
		trainer.setModel(model);
		trainer.setMat(mat);
		trainer.train();
		
		return model;
	}

	private double evaluate(CFModel model) {
		double error = 0.0;
		
		Iter it = testMat.iterateMatrix();
		Double sample;
		while (!(sample = it.next()).equals(Double.MIN_VALUE)) {
			double estimate = model.predictVal(it.getU(), it.getI());
			error += (sample - estimate) * (sample - estimate);
		}
		return error;
	}
}
