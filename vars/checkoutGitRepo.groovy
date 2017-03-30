#!/usr/bin/groovy

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    checkout([$class: 'GitSCM',
            branches: [[name: config.branch]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                    [$class: 'RelativeTargetDirectory',
                            relativeTargetDir: config.targetDir ?: '.']
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
