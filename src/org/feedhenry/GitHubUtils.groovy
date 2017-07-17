#!/usr/bin/groovy
package org.feedhenry

import groovy.json.JsonSlurperClassic

String ghApiRequest(String endpoint, String httpMethod = 'GET', String postData = null, String credentialsId = 'githubautomatron') {
    def utils = new Utils()
    return utils.doCurlCmd("https://api.github.com${endpoint}", httpMethod, postData, credentialsId)
}

String ghBranchProtectionApiRequest(String branchName, String ghOrg, String ghRepo, String httpMethod = 'GET', String postData = null, String credentialsId = 'githubautomatron') {
    def branchStatus = "${branchName} on ${ghRepo} in ${ghOrg} : UNKNOWN"
    def ghStatusRaw = ghApiRequest("/repos/${ghOrg}/${ghRepo}/branches/${branchName}/protection", httpMethod, postData, credentialsId)
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
