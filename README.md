# CodeReviewerRecommendation

## How to run the crawler tool?
The main class of crawling data of GitHub projects is ExtractReviewData.GitHubAPICrawler.
This class can help you crawl the data of a project. Each category of data is saved as a json file.
Specifically, five categories of data are crawled.
### issue_comments
issue_comments.json includes all comments of issues in a project. 
### issues
issues.json is about basic information of issues of a project.
### prFileMap
prFileMap.json records the mapping from a pull reqeust to the code files it changes.
### prs
prs.json records the basics of pull reqeusts of a project.
### pull_request_comments
pull_request_comments.json records all comments of pull requests in a project.

## How to run the experiments?
The main class of experiments is ReviewerRecommendation.ReReEntrance. The program of the experiment loads the data crawled by "ExtractReviewData.GitHubAPICrawler" and runs algorithms to calculate the precision and recall.
If you want to try a new algorithm, you can add a line of code such as "ent.startEvalByFmeasure(new yourAlgorithms(), methodNum);" in the main method in ReReEntrance class.

## Notes
path.properties is a configuration file where you need to set your machine environment(linux, windows), path of projects' data.

