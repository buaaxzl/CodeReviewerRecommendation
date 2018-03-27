package ReviewerRecommendation.DataProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;

import ReviewerRecommendation.ReReEntrance;

public class DataPreparation {

	public static Object loadData(String project, String file, IDataParser parser) {

		ResourceBundle resource = ResourceBundle.getBundle("path");
		String filePath;
		if (ReReEntrance.platform.equals("windows"))
			filePath = resource.getString("WINDOWSBASEPATH") + project + "\\" + file;
		else
			filePath = resource.getString("LINUXBASEPATH") + project + "/" + file;
		
		File cond = new File(filePath);
		if (!cond.exists())
			return null;
		
		try (BufferedReader bReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), "utf-8"));) {
			String jsonString = "";
			String line = null;
			while ((line = bReader.readLine()) != null) {
				jsonString += line;
			}
			return parser.parse(jsonString);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Repository repo = null;
	private Map<String, PullRequest> prs = null;
	private Map<String, List<CommitComment>> prComments = null;
	private Map<String, Issue> issues = null;
	private Map<String, List<Comment>> issueComments = null;
	private Map<String, RepositoryCommit> commits = null;
	private Map<String, List<CommitComment>> commitComments = null;
	private Map<String, List<CommitFile>> prFiles = null;
	private Map<String, List<RepositoryCommit>> prCommits = null;
	
	public Repository getRepo() {
		return repo;
	}

	public Map<String, PullRequest> getPrs() {
		return prs;
	}

	public Map<String, List<CommitComment>> getPrComments() {
		return prComments;
	}

	public Map<String, Issue> getIssues() {
		return issues;
	}

	public Map<String,List<Comment>> getIssueComments() {
		return issueComments;
	}

	public Map<String, RepositoryCommit> getCommits() {
		return commits;
	}

	public Map<String, List<CommitComment>> getCommitComments() {
		return commitComments;
	}
	
	public Map<String, List<CommitFile>> getPrFiles() {
		return prFiles;
	}
	
	public Map<String, List<RepositoryCommit>> getPrCommits() {
		return prCommits;
	}
	
	@SuppressWarnings("unchecked")
	public void prepare(String project) {
		
//		repo = (Repository)DataPreparation.loadData(
//				project, "repos.json", new RepoDataProcess());
		
		prs = (Map<String, PullRequest>)DataPreparation.loadData(
				project, "prs.json", new PullRequestDataProcess());
		
		prComments = 
				(Map<String, List<CommitComment>>)DataPreparation.loadData(
				project, "pull_request_comments.json", new PRCommentsDataProcess());
		
		prFiles = 
				(Map<String, List<CommitFile>>)DataPreparation.loadData(
				project, "prFileMap.json", new PRFilesDataProcess());
		
//		prCommits = 
//				(Map<String, List<RepositoryCommit>>)DataPreparation.loadData(
//				project, "prCommits.json", new PRCommitsDataProcess());
		
		issues = (Map<String, Issue>)DataPreparation.loadData(
				project, "issues.json", new IssuesDataProcess());
		
		issueComments = 
				(Map<String, List<Comment>>)DataPreparation.loadData(
				project, "issue_comments.json", new IssueCommentsDataProcess());
		
//		commits = (Map<String, RepositoryCommit>)DataPreparation.loadData(
//				project, "commits.json", new CommitDataProcess());
		
//		commitComments = 
//				(Map<String, List<CommitComment>>)DataPreparation.loadData(
//				project, "commit_comments.json", new CommitCommentDataProcess());
	}
	
	public static void main(String[] args) {
		DataPreparation dp = new DataPreparation();
		dp.prepare("netty-netty");
		System.out.println(dp.getPrComments().get("5613").size());

		System.out.println(dp.getPrFiles().containsKey("5613"));
	}
}
