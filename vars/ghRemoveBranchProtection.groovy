#!/usr/bin/groovy
import org.feedhenry.GitHubUtils

def call(String branchName, String ghOrg, String ghRepo) {
    def ghUtils = new GitHubUtils()
    println "Remove branch protection for ${branchName} on ${ghRepo} in ${ghOrg}"
    return ghUtils.ghBranchProtectionApiRequest(branchName, ghOrg, ghRepo, "DELETE")
}
