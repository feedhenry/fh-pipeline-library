#!/usr/bin/groovy

def call(commitMsg, baseBranch) {
    def commit = sh(returnStdout: true, script: "git log ${baseBranch}..HEAD --grep '${commitMsg}' --pretty=format:\"%H\"").trim()
    if (commit) {
        print "Deleting commit ${commit}"
        sh "git rebase --onto ${commit}^ ${commit}"
    } else {
        print "No commit found for message ${commitMsg} since ${baseBranch}!"
    }
}
