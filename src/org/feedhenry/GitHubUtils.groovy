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
            url: url,
            validResponseCodes: '100:399,404'
    return response
}

String ghBranchProtectionApiRequest(String branchName, String ghOrg, String ghRepo, String httpMethod = 'GET', String requestBody = null, String credentialsId = 'githubautomatron') {
    def branchStatus = "UNKNOWN"
    try {
        def response = ghApiRequest("/repos/${ghOrg}/${ghRepo}/branches/${branchName}/protection",
                httpMethod,
                requestBody,
                credentialsId,
                [[name: 'Accept', value: 'application/vnd.github.loki-preview+json']]
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
