#!/usr/bin/groovy

def call(String gitRepoUrl, String checkoutGitBranch, String checkoutDir, String tagName, String tagGitRef = 'HEAD', Closure body = {}) {
    checkoutGitRepo {
        repoUrl = gitRepoUrl
        branch = checkoutGitBranch
        targetDir = checkoutDir
    }
    dir(checkoutDir) {
        sshagent(['jenkinsgithub']) {
            //How do we get this from the global config? Is sshagent the only way to do this currently or is there a git pipeline step?
            sh "git config user.name \"Jenkins\""
            sh "git config user.email \"jenkins@wendy.feedhenry.net\""

            body()

            def existingTagCommitHash = sh(returnStdout: true, script: "git ls-remote origin refs/tags/${tagName} | cut -f 1").trim()
            def tagCommitHash = sh(returnStdout: true, script: "git rev-parse ${tagGitRef}").trim()

            if (existingTagCommitHash) {
                if (existingTagCommitHash == tagCommitHash) {
                    print "Tag ${tagName} already exists and tagCommitHash(${tagCommitHash}) is the same, can continue!"
                } else {
                    print "Tag ${tagName} already exists but tagCommitHash(${tagCommitHash}) is not the same, can't continue!"
                    sh('exit 1')
                }
            } else {
                print "Creating new tag ${tagName} at ${tagCommitHash}"
                sh "git tag ${tagName} ${tagCommitHash}"
            }

            if (params.dryRun) {
                print "Would push ${checkoutGitBranch} to ${gitRepoUrl}"
                print "Would push ${tagName} to ${gitRepoUrl}"
            } else {
                sh "git push origin HEAD:${checkoutGitBranch}"
                sh "git push origin ${tagName}"
            }
        }
    }
}
