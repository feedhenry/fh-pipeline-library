#!/usr/bin/groovy

def call(gitRepoUrl, gitRef, checkoutDir, branchName, tagName, dryRun, body) {
    checkoutGitRepo {
        repoUrl = gitRepoUrl
        branch = gitRef
        targetDir = checkoutDir
    }
    dir(checkoutDir) {
        sshagent(['jenkinsgithub']) {
            //How do we get this from the global config? Is sshagent the only way to do this currently or is there a git pipeline step?
            sh "git config user.name \"Jenkins\""
            sh "git config user.email \"jenkins@wendy.feedhenry.net\""

            def latestCommitHash = sh(returnStdout: true, script: "git log -n 1 --pretty=format:\"%H\"").trim()
            def existingTagCommitHash = sh(returnStdout: true, script: "git ls-remote origin refs/tags/${tagName} | cut -f 1").trim()
            def existingBranchCommitHash = sh(returnStdout: true, script: "git ls-remote origin refs/heads/${branchName} | cut -f 1").trim()

            if (existingBranchCommitHash) {
                if (existingBranchCommitHash == latestCommitHash) {
                    print "Branch ${branchName} already exists and latestCommitHash(${latestCommitHash}) is the HEAD, can continue!"
                    sh "git checkout ${branchName}"
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

            if (existingTagCommitHash) {
                if (existingTagCommitHash == latestCommitHash) {
                    print "Tag ${tagName} already exists and latestCommitHash(${latestCommitHash}) is the same, can continue!"
                } else {
                    print "Tag ${tagName} already exists but latestCommitHash(${latestCommitHash}) is not the same, can't continue!"
                    sh('exit 1')
                }
            } else {
                print "Creating new tag ${tagName} at ${latestCommitHash}"
                sh "git tag ${tagName}"
            }

            if (dryRun) {
                print "Would push ${branchName} to ${gitRepoUrl}"
                print "Would push ${tagName} to ${gitRepoUrl}"
            } else {
                sh "git push origin ${branchName}"
                sh "git push origin ${tagName}"
            }
        }
    }
}