#!/usr/bin/groovy

/*
 * To be able to use the method exposed here, the following script signatures
 * must be approved:
 *
 * method org.kohsuke.github.GHUser isMemberOf org.kohsuke.github.GHOrganization
 * method org.kohsuke.github.GitHub getOrganization java.lang.String
 * method org.kohsuke.github.GitHub getUser java.lang.String
 * method org.kohsuke.github.GitHubBuilder build
 * method org.kohsuke.github.GitHubBuilder withOAuthToken java.lang.String java.lang.String
 * new org.kohsuke.github.GitHubBuilder
 *
 */
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHUser
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.GHPullRequest
import java.util.regex.Pattern

def getLabels(int pr, String repository, String org, String gitHubCredentialsId) {
    node {
        withCredentials([usernamePassword(
                    credentialsId: gitHubCredentialsId,
                    passwordVariable: 'GITHUB_PASSWORD',
                    usernameVariable: 'GITHUB_USERNAME')]) {
            final GitHub gitHub = new GitHubBuilder()
                .withOAuthToken(env.GITHUB_PASSWORD, env.GITHUB_USERNAME)
                .build()

            final GHOrganization ghOrganization = gitHub.getOrganization(org)
            def labels = ghOrganization.getRepository(repository).getPullRequest(pr).getLabels();

            return labels.collect {it.getName()}
        }
    }
}

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def prnfo = getPullRequestInfo()
    def org = config.organization ?: prnfo["org"]
    def repo = config.repository ?: prnfo["repo"]
    def pr = config.pr ?: prnfo["id"]
    def credentials = config.credentials ?: "githubjenkins"
   
    try {
      return getLabels(Integer.parseInt(pr), repo, org, credentials) 
    } catch (err) {
      print "Problem while getting labels from ${org} ${repo} ${pr}, returning empty list"
      return []
    }
}
