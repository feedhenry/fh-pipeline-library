#!/usr/bin/groovy
package org.feedhenry

import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonSlurperClassic
import org.kohsuke.github.GHCommitState
import org.kohsuke.github.GHCommitStatus
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.PagedIterable

def ghApiRequest(String endpoint, String httpMethod = 'GET', String requestBody = null, String credentialsId = 'githubautomatron', customHeaders = [], validResponseCodes = '100:399') {
    //Requires this plugin to be installed https://plugins.jenkins.io/http_request
    def url = "https://api.github.com${endpoint}"
    response = httpRequest httpMode: httpMethod,
            authentication: credentialsId,
            contentType: 'APPLICATION_JSON',
            requestBody: requestBody,
            customHeaders: customHeaders,
            url: url,
            validResponseCodes: validResponseCodes
    return response
}

String ghBranchProtectionApiRequest(String branchName, String ghOrg, String ghRepo, String httpMethod = 'GET', String requestBody = null, String credentialsId = 'githubautomatron') {
    def branchStatus = "UNKNOWN"
    try {
        def response = ghApiRequest("/repos/${ghOrg}/${ghRepo}/branches/${branchName}/protection",
                httpMethod,
                requestBody,
                credentialsId,
                [[name: 'Accept', value: 'application/vnd.github.loki-preview+json']],
                '100:399,404'
        )
        println("Status: " + response.status)
        println("Content: " + response.content)

        if (response.content) {
            def ghStatus = new JsonSlurperClassic().parseText response.content
            if (ghStatus && ghStatus['message']) {
                branchStatus = ghStatus['message']
            } else {
                branchStatus = "Branch Protected"
            }
        }
    } catch (Exception e) {
        branchStatus = "EXCEPTION"
    }
    return "${branchName} on ${ghRepo} in ${ghOrg} : ${branchStatus}"
}

@NonCPS
GHPullRequest ghGetPullRequest(GHRepository repo, String head, String base, GHIssueState state) {
    PagedIterable<GHPullRequest> pullRequests = repo.queryPullRequests()
            .head(head)
            .base(base)
            .state(state)
            .list()
    return pullRequests[0]
}

@NonCPS
GHPullRequest ghFindOrCreatePullRequest(GHRepository repo, String head, String base, String title, String body) {
    GHPullRequest pr = ghGetPullRequest(repo, head, base, GHIssueState.OPEN)
    if (pr) {
        println "Found already open PR on ${repo.getName()} head:${head} base:${base} - ${pr.getHtmlUrl()}"
    } else {
        pr = repo.createPullRequest(title, head, base, body)
        println "Opened new PR on ${repo.getName()} head:${head} base:${base} - ${pr.getHtmlUrl()}"
    }
    return pr
}

@NonCPS
GHPullRequest ghGetPullRequestFromUrl(GitHub gitHub, String prUrl) {
    URL url = new URL(prUrl)
    String[] pathSegments = url.path.trim().split('/') - '' - 'pull'
    String ghOwner = pathSegments[0]
    String ghRepo = pathSegments[1]
    String ghPrNumber = pathSegments[2]
    GHRepository repo = gitHub.getRepository("${ghOwner}/${ghRepo}")
    return repo.getPullRequest(ghPrNumber as int)
}

@NonCPS
GHCommitStatus ghUpdatePrCommitStatus(GHPullRequest pr, GHCommitState state, String targetUrl, String description, String context) {
    return pr.getRepository().createCommitStatus(pr.getHead().getSha(), state, targetUrl, description, context)
}

@NonCPS
GHCommitStatus ghUpdatePrCommitStatus(GitHub gitHub, String prUrl, GHCommitState state, String targetUrl, String description, String context) {
    return ghUpdatePrCommitStatus(ghGetPullRequestFromUrl(gitHub, prUrl), state, targetUrl, description, context)
}
