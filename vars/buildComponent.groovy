#!/usr/bin/groovy

def call(name, sha1, projectName, config) {
    def repoName = config['repoName']
    def repoDir = config['repoDir']
    def distCmd = config['distCmd']
    def gitHubOrg = config['gitHubOrg']
    def jobParams = [[$class: 'StringParameterValue', name: 'componentName', value: name],
            [$class: 'StringParameterValue', name: 'sha1', value: sha1],
            [$class: 'StringParameterValue', name: 'gitHubOrg', value: gitHubOrg],
            [$class: 'StringParameterValue', name: 'repoName', value: repoName],
            [$class: 'StringParameterValue', name: 'repoDir', value: repoDir]
    ]
    if(distCmd) {
        jobParams << [$class: 'StringParameterValue', name: 'distCmd', value: distCmd]
    }

    build job: projectName, parameters: jobParams
}
