#!/usr/bin/groovy
import groovy.json.JsonSlurperClassic

def call(componentConfigs, configGitRepo, configGitRef) {
    node {
        step([$class: 'WsCleanup'])

        checkoutGitRepo {
            repoUrl = configGitRepo
            branch = configGitRef
            targetDir = 'product_releases'
        }

        dir('product_releases') {
            componentsJson = readFile 'COMPONENTS.json'
            def object = new JsonSlurperClassic().parseText componentsJson
            def components = object['components']

            for (i = 0; i < components.size(); i++) {
                def index = i
                if (components[index]['type'] == 'platform') {
                    def config = components[index]
                    def componentName = config['name']
                    componentConfigs[componentName] = [:]
                    componentConfigs[componentName]['gitHubOrg'] = params.gitHubOrg ?: config['org']
                    componentConfigs[componentName]['repoName'] = config['repoName'] ?: componentName
                    componentConfigs[componentName]['baseBranch'] = config['baseBranch'] ?: 'master'
                    componentConfigs[componentName]['repoDir'] = config['repoDir'] ?: ''
                    componentConfigs[componentName]['cookbook'] = config['cookbook'] ?: componentName
                    componentConfigs[componentName]['buildType'] = config['buildType'] ?: 'node'
                    componentConfigs[componentName]['distCmd'] = config['distCmd']
                    componentConfigs[componentName]['labels'] = config?.labels ?: {}
                    componentConfigs[componentName]['type'] = config['type']
                    componentConfigs[componentName]['buildJobName'] = config['buildJobName'] ?: "build_any_jenkinsfile"
                    componentConfigs[componentName]['gitUrl'] = "git@github.com:${componentConfigs[componentName]['gitHubOrg']}/${componentConfigs[componentName]['repoName']}.git"
                    componentConfigs[componentName]['gitHubUrl'] = "https://github.com/${componentConfigs[componentName]['gitHubOrg']}/${componentConfigs[componentName]['repoName']}"
                }
            }
        }
    }
}
