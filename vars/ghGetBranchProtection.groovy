#!/usr/bin/groovy
import org.feedhenry.GitHubUtils

def call(String branchName, String ghOrg, String ghRepo) {
    def ghUtils = new GitHubUtils()
    println "Get branch protection for ${branchName} on ${ghRepo} in ${ghOrg}"
    return ghUtils.ghBranchProtectionApiRequest(branchName, ghOrg, ghRepo)
}
