#!/usr/bin/groovy

def call(String name, String version, String dockerHubOrg, String dockerHubRepo = name, String dockerSecret = "dockerhub", String fromDir = './docker', Closure body = {}) {
    def pushConfig = [
            "kind": "DockerImage",
            "name": "docker.io/${dockerHubOrg}/${dockerHubRepo}:${version}-${env.BUILD_NUMBER}"
    ]

    openshiftEnsureBinaryBuild("${name}-bc", pushConfig, dockerSecret)
    body()
    sh "oc start-build ${name}-bc --follow=true --from-dir ${fromDir}"
}
