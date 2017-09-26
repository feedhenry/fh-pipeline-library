#!/usr/bin/groovy

def call(String name, String version, String dockerHubOrg, String dockerHubRepo = name, String credentialId = "dockerhubjenkins", String fromDir = './docker', Closure body = {}) {

    echo "Deprecated: this should no longer be needed, use `buildWithDockerStrategy` instead"

    final Map params = [
        fromDir: fromDir,
        buildConfigName: name,
        imageRepoSecret: "dockerhub",
        outputImage: "docker.io/${dockerHubOrg}/${dockerHubRepo}:${version}-${env.BUILD_NUMBER}"
    ]

    buildWithDockerStrategy params
}
