#!/usr/bin/groovy

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

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
