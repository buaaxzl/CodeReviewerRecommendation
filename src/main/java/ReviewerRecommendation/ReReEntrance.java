package ReviewerRecommendation;

import ReviewerRecommendation.Algorithms.Activeness.Activeness;
import ReviewerRecommendation.Algorithms.Collaboration.CommentNetwork;
import ReviewerRecommendation.Algorithms.FPS.FPS;
import ReviewerRecommendation.Algorithms.MixedAlgo.IRandCN;
import ReviewerRecommendation.Algorithms.TIE.TIEComposer;
import ReviewerRecommendation.Algorithms.RSVD.recommend.SVDRecommendationFile;
import ReviewerRecommendation.Algorithms.RSVD.recommend.SVDRecommendationPR;
import ReviewerRecommendation.Algorithms.RSVD.recommend.SVDRecommendationUser;
import ReviewerRecommendation.Algorithms.RecommendAlgo;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/*
*  The entrance of experiment
 */
public class ReReEntrance {

	private String repo = "netty";
	private String owner = "netty";
	public static String platform = "windows";

	//top-k
	public int k = 10;

    //set the time locality of train set. Pull requests within M days are used as training
	public int M = 90;

    //a parameter about time locality of training data.
    //Used to set the minimum number of PRs to participate in training.
	public int N = 500;

    //the stage size of staged-algorithms
	public int cnt = 100;

    public DataService dataService = new DataService();

	public void setK(int k) {
		this.k = k;
	}
	public int getK() {
		return k;
	}
	
	public String getRepo() {
		return repo;
	}

	public String getOwner() {
		return owner;
	}

	public DataService getDataService() {
	    return dataService;
    }


	public ReReEntrance(String repo, String owner, String platform) {
		this.repo = repo;
		this.owner = owner;
		ReReEntrance.platform = platform;

        dataService.preProcessData(owner, repo);
	}

	public int temporalLocality(int cur) {
		int ret = 0;
		int cnt = 0;
		Date now = dataService.getPrList().get(cur).getCreatedAt();
		long nowMilli = now.getTime();
		for (int i = cur - 1; i >= 0; i--) {
			
			Date past = dataService.getPrList().get(i).getCreatedAt();
			long pastMilli = past.getTime();
			if (nowMilli - pastMilli > M*24*3600*1000) {
				ret = i;
				cnt ++;
				break;
			}
			cnt ++;
		}
		
		if (cnt < N) {
			for (int i = ret-1; i >= 0; i--) {
				cnt ++;
				if (cnt >= N)  {
					ret = i;
					break;
				}
			}
		}
		return ret;
	}
	

	public void startEvalByFmeasure(RecommendAlgo ra, int methodNum) {
		System.out.println(getOwner() + " " + getRepo() + " " + ra.getClass());
		
		int len = dataService.getPrList().size();
		int num = 0;
		double[] precision = new double[10];
		double[] recall = new double[10];
		double rankMetric1 = 0.0;
		double rankMetric2 = 0.0;
		double mrr = 0.0;
		methodNum ++;
		
		for (int p = 0; p < 10; p++) {
			precision[p] = 0;
			recall[p] = 0;
		}
		setK(10);
		
		int start = len - 600;
		while (start + cnt <= len) {

            // These algorithms are staged algorithms, so their models need to rebuild.
			if (ra instanceof IRandCN) {
				CommentNetwork cn = new CommentNetwork();
				((IRandCN)ra).setCn(cn);
			}
			
			if (ra instanceof CommentNetwork) {
				ra = new CommentNetwork();
			}
			
			if (ra instanceof SVDRecommendationPR) {
				ra = new SVDRecommendationPR();
			}
			
			if (ra instanceof SVDRecommendationUser) {
				ra = new SVDRecommendationUser();
			}

			if (ra instanceof SVDRecommendationFile) {
			    ra = new SVDRecommendationFile();
            }
			
			for (int i = start; i < start+cnt; i++) {
				List<String> reviewers = ra.recommend(i, this);
				try {
					if (reviewers == null || reviewers.size() == 0) 
						throw new RuntimeException("recommend nobody");
					List<String> trueReviewers = dataService.getCodeReviewers(i);
					if (trueReviewers == null || trueReviewers.size() == 0) 
						throw new RuntimeException("doestn't have reviewers");
					
					int cnt = 0;
					int flag = 0;
					for (int j = 0; j < 10; j++) {
						if (trueReviewers.contains(reviewers.get(j)))
							cnt ++;
						
						if (flag == 0 && cnt == 1) {
							mrr += (1.0 / (j+1.0));
							flag = 1;
						}
						
						precision[j] += ((cnt+0.0) / (j+1.0));
						recall[j] += ((cnt+0.0) / (trueReviewers.size()+0.0));
					}

				} catch (RuntimeException e) {
					num --;
				}
				num ++;
			}
			start = start + cnt;
		}
			
		outputResult(num, precision, recall, mrr, rankMetric1, rankMetric2, methodNum);
	}
	
	private void outputResult(int num, double[] precision, double[] recall, double mrr,
			double rankMetric1, double rankMetric2, int methodNum) {

        CSVWriter csvWriterOfFmeasure = null;
        CSVWriter csvWriterOfPrecisionRecall = null;
        CSVWriter csvWriterOfMRR = null;

        String path;
        ResourceBundle resource = ResourceBundle.getBundle("path");

        if (platform.equals("linux"))
            path = resource.getString("LINUXBASEPATH");
        else
            path = resource.getString("WINDOWSBASEPATH");

        try {
            File file1 = new File(path + "Fmeasure-" + getOwner() + "-" + getRepo() + ".csv");
            if (!file1.exists()) {
                file1.createNewFile();
            }
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(file1, true));
            csvWriterOfFmeasure = new CSVWriter(writer1, ',');

            File file2 = new File(path + "PrecisionRecall-" + getOwner() + "-" + getRepo() + ".csv");
            if (!file2.exists()) {
                file2.createNewFile();
            }
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(file2, true));
            csvWriterOfPrecisionRecall = new CSVWriter(writer2, ',');

            File file4 = new File(path + "MRR-" + getOwner() + "-" + getRepo() + ".csv");
            if (!file4.exists()) {
                file4.createNewFile();
            }
            BufferedWriter writer4 = new BufferedWriter(new FileWriter(file4, true));
            csvWriterOfMRR = new CSVWriter(writer4, ',');

        } catch (Exception e) {
            System.out.println("error when write the result！");
            e.printStackTrace();
            return;
        }

        String[] lineOfFmeasure = new String[10];
        String[][] PrecisionRecall = new String[10][2];
        String[] lineOfMRR = new String[1];

		for (int i = 0; i < 10; i++) {
			lineOfFmeasure[i] = Double.toString(
					(2*(precision[i]/num)*(recall[i]/num))/((precision[i]/num)+(recall[i]/num)));

			PrecisionRecall[i][0] = Double.toString(precision[i]/num);
			PrecisionRecall[i][1] = Double.toString(recall[i]/num);
		}
		lineOfMRR[0] = Double.toString(mrr/(num+0.0));


        //写入文件
        csvWriterOfFmeasure.writeNext(new String[]{methodNum+"","*","*"});
        csvWriterOfPrecisionRecall.writeNext(new String[]{methodNum+"","*","*"});
        csvWriterOfMRR.writeNext(new String[]{methodNum+"","*","*"});

		writeFmeasure(csvWriterOfFmeasure, lineOfFmeasure);
        writePrecisionRecallAndRankMetric(csvWriterOfPrecisionRecall, csvWriterOfMRR, PrecisionRecall, lineOfMRR);

        //关闭写入
        try {
            csvWriterOfFmeasure.close();
            csvWriterOfPrecisionRecall.close();
            csvWriterOfMRR.close();
            //        csvWriterOfRankMetric.close();
        } catch (Exception e) {
            System.out.println("error when close csvwriter！");
            e.printStackTrace();
        }

/**************print results to screen************************/
//		for (int i = 0; i < 10; i++) {
//			System.out.println("topK : " + (i+1));
//			System.out.println("total : " + num);
//			System.out.println("Precision: " + precision[i]/num);
//			System.out.println("Recall : " + recall[i]/num);
//			System.out.println("rankMetric old: " + rankMetric1/num);
//			System.out.println("rankMetric new: " + rankMetric2/num);
//			System.out.println("F-Measure: " +
//					(2*(precision[i]/num)*(recall[i]/num))/((precision[i]/num)+(recall[i]/num)));
//			System.out.println();
//		}
//		System.out.println("MRR: " + (mrr/(num+0.0)));
//		System.out.println("----");
/**************************************************/
	}

	private void writeFmeasure(CSVWriter csvWriterOfFmeasure, String[] lineOfFmeasure) {
		csvWriterOfFmeasure.writeNext(lineOfFmeasure);

		try {
			csvWriterOfFmeasure.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writePrecisionRecallAndRankMetric(CSVWriter csvWriterOfPrecisionRecall,
                                                   CSVWriter csvWriterOfMRR,
                                                   String[][] PrecisionRecall,
                                                   String[] lineOfMRR) {

	    List<String[]> tmp = new ArrayList<String[]>();
		for (int i = 0; i < PrecisionRecall.length; i++)
			tmp.add(PrecisionRecall[i]);
		csvWriterOfPrecisionRecall.writeAll(tmp);

        //write MRR
		csvWriterOfMRR.writeNext(lineOfMRR);
		
		try {
			csvWriterOfPrecisionRecall.flush();
			csvWriterOfMRR.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {

		String[][] projects = {
				{"angular.js","angular"},
				{"netty","netty"},
				{"salt","saltstack"},
				{"ipython", "ipython"},
				{"symfony", "symfony"},
		};

		int start = 0;
		int end = projects.length;

		if (args.length == 2) {
			start = Integer.parseInt(args[1]);
			end = start + 1;
		}

		for (int j = start; j < end; j++) {
			String repo = projects[j][0];
			String owner = projects[j][1];

			System.out.println(owner + " " + repo);

			String platform = ResourceBundle.getBundle("path").getString("PLATFORM");

			ReReEntrance ent = new ReReEntrance(repo, owner, platform);

            ent.startEvalByFmeasure(new FPS(), 1);
            ent.startEvalByFmeasure(new IRandCN(), 2);
            ent.startEvalByFmeasure(new TIEComposer(), 3);
            ent.startEvalByFmeasure(new Activeness(), 4);
            ent.startEvalByFmeasure(new SVDRecommendationPR(), 5);

            System.out.println("*********************");
		}
	}
}
