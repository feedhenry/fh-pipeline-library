#!/usr/bin/groovy

def call(String gitRepoUrl, String gitRef, String checkoutDir, String branchName, boolean useExistingBranch = false, Closure body = {}) {
    checkoutGitRepo {
        repoUrl = gitRepoUrl
        branch = gitRef
        targetDir = checkoutDir
        shallow = true
    }
    dir(checkoutDir) {
        def latestCommitHash = sh(returnStdout: true, script: "git log -n 1 --pretty=format:\"%H\"").trim()
        def existingBranchCommitHash = sh(returnStdout: true, script: "git ls-remote origin refs/heads/${branchName} | cut -f 1").trim()

        if (existingBranchCommitHash) {
            sh "git checkout ${branchName}"
            if(useExistingBranch) {
                print "Branch ${branchName} already exists and useExistingBranch is true, can continue!"
            } else if (existingBranchCommitHash == latestCommitHash) {
                print "Branch ${branchName} already exists and latestCommitHash(${latestCommitHash}) is the HEAD, can continue!"
            } else {
                print "Branch ${branchName} already exists but latestCommitHash(${latestCommitHash}) is not the HEAD, can't continue!"
                sh('exit 1')
            }
        } else {
            print "Creating new branch ${branchName} at ${latestCommitHash}"
            sh "git checkout -b ${branchName}"
        }

        body()

        latestCommitHash = sh(returnStdout: true, script: "git log -n 1 --pretty=format:\"%H\"").trim()

        if (params.dryRun) {
            print "Would push ${branchName} to ${gitRepoUrl}"
        } else {
            sh "git push origin ${branchName}"
        }
    }
}
