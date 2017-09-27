#!/usr/bin/groovy

def call(branchName, pushOnCreate = false) {
    String remoteBranchCommit = sh(returnStdout: true, script: "git ls-remote origin refs/heads/${branchName} | cut -f 1").trim()

    if (remoteBranchCommit) {
        sh "git checkout ${branchName}"
    } else {
        sh "git checkout -b ${branchName}"
        if (pushOnCreate) {
            if (params.dryRun) {
                String gitRepoUrl = sh(returnStdout: true, script: 'git config --get remote.origin.url').trim()
                print "Would push ${branchName} to ${gitRepoUrl}"
            } else {
                sh "git push origin ${branchName}"
            }
        }
    }
}
