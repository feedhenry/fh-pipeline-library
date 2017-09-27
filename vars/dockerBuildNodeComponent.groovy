#!/usr/bin/groovy

def call(String name, String dockerHubOrg = "feedhenry", String dockerHubRepo = name, String credentialId = "dockerhubjenkins") {
    sh "cp ./dist/fh-*x64.tar.gz docker/"

    final String version = getBaseVersionFromPackageJson()
    final String tag = "${version}-${env.BUILD_NUMBER}"
    final Map params = [
        fromDir: "./docker",
        buildConfigName: name,
        imageRepoSecret: "dockerhub",
        outputImage: "docker.io/${dockerHubOrg}/${dockerHubRepo}:${tag}"
    ]

    buildWithDockerStrategy params
}
