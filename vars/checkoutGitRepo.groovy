#!/usr/bin/groovy

def checkoutWithConfig(config) {
   def noTags = config.noTags  ?: false
   def shallow = config.shallow  ?: false
   
   checkout([$class: 'GitSCM',
            branches: [[name: config.branch]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                    [$class: 'RelativeTargetDirectory', relativeTargetDir: config.targetDir ?: '.'],
                    [$class: 'CloneOption', noTags: noTags, reference: '', shallow: shallow]
            ],
            submoduleCfg: [],
            userRemoteConfigs: [
                    [
                            credentialsId: 'jenkinsgithub',
                            url: config.repoUrl
                    ]
            ]
    ])
}

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def backupBranch = config.backupBranch  ?: false

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
