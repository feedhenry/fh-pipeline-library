#!/usr/bin/groovy

def call(String name, String version, String dockerHubOrg, String dockerHubRepo = name, String credentialId = "dockerhubjenkins", String fromDir = './docker', Closure body = {}) {
    openshiftEnsureDockerhubCredentials(dockerHubOrg, credentialId)

    def pushConfig = [
            "kind": "DockerImage",
            "name": "docker.io/${dockerHubOrg}/${dockerHubRepo}:${version}-${env.BUILD_NUMBER}"
    ]

    openshiftEnsureBinaryBuild("${name}-bc", pushConfig)
    body()
    sh "oc start-build ${name}-bc --follow=true --from-dir ${fromDir}"
}
