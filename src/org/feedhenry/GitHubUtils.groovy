#!/usr/bin/groovy
package org.feedhenry

import groovy.json.JsonSlurperClassic

def ghApiRequest(String endpoint, String httpMethod = 'GET', String requestBody = null, String credentialsId = 'githubautomatron', customHeaders = []) {
    //Requires this plugin to be installed https://plugins.jenkins.io/http_request
    def url = "https://api.github.com${endpoint}"
    response = httpRequest httpMode: httpMethod,
            authentication: credentialsId,
            contentType: 'APPLICATION_JSON',
            requestBody: requestBody,
            customHeaders: customHeaders,
            url: url
    return response
}

String ghBranchProtectionApiRequest(String branchName, String ghOrg, String ghRepo, String httpMethod = 'GET', String requestBody = null, String credentialsId = 'githubautomatron') {
    def branchStatus = "${branchName} on ${ghRepo} in ${ghOrg} : UNKNOWN"
    def response = ghApiRequest("/repos/${ghOrg}/${ghRepo}/branches/${branchName}/protection",
            httpMethod,
            requestBody,
            credentialsId,
            [[name: 'Accept', value: 'application/vnd.github.loki-preview+json']]
    )
    def ghStatusRaw = response.content

    println("Status: " + response.status)
    println("Content: " + response.content)

    if (ghStatusRaw) {
        def ghStatus = new JsonSlurperClassic().parseText ghStatusRaw
        if (ghStatus && ghStatus['message']) {
            branchStatus = "${branchName} on ${ghRepo} in ${ghOrg} : ${ghStatus['message']}"
        } else {
            branchStatus = "${branchName} on ${ghRepo} in ${ghOrg} : Branch Protected"
        }
    }
    return branchStatus
}
