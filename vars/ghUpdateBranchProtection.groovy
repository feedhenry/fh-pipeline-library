#!/usr/bin/groovy
import org.feedhenry.GitHubUtils

def call(String branchName, String ghOrg, String ghRepo, String branchProtectionSettings = '{"required_status_checks": {"strict": true, "contexts": []},"enforce_admins": true, "restrictions": null}') {
    def ghUtils = new GitHubUtils()
    println "Update branch protection for ${branchName} on ${ghRepo} in ${ghOrg}"
    return ghUtils.ghBranchProtectionApiRequest(branchName, ghOrg, ghRepo, "PUT", branchProtectionSettings)
}
