#!/usr/bin/groovy

/**
 * @param config {
 *     noTags = pull all tags from remote repo or not?
 *     shallow = shallow copy of remote repository
 *     localBranch = clone remote branch into local branch
 *     targetDir =  what directory files should be cloned in
 *     remoteBranches = subset of remote branches you want to pull
 * }
 * @return void
 */
void checkoutWithConfig(config) {
    def noTags = config.noTags ?: false
    def shallow = config.shallow ?: false
    def localBranch = config.localBranch ?: 'master'
    def relativeTargetDir = config.targetDir ?: '.'
    def remoteBranches = config.remoteBranches ?: [
            [
                    name: config.branch ?: '*/master'
            ]
    ]
    def userRemoteConfigs = config.userRemoteConfigs ?: [
            [
                    credentialsId: 'jenkinsgithub',
                    url          : config.repoUrl,
            ]
    ]

    checkout([$class                           : 'GitSCM',
              branches                         : remoteBranches,
              doGenerateSubmoduleConfigurations: false,
              extensions                       : [
                      [$class: 'LocalBranch', localBranch: localBranch],
                      [$class: 'RelativeTargetDirectory', relativeTargetDir: relativeTargetDir],
                      [$class: 'CloneOption', noTags: noTags, reference: '', shallow: shallow]
              ],
              submoduleCfg                     : [],
              userRemoteConfigs                : userRemoteConfigs
    ])
}

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def backupBranch = config.backupBranch ?: false

    try {
        checkoutWithConfig(config)
    } catch (Exception e) {
        if (backupBranch) {
            config.branch = backupBranch
            checkoutWithConfig(config)
        } else {
            throw e
        }
    }
}
