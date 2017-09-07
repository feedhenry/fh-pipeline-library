#!/usr/bin/groovy

def call(name, dockerHubOrg = "feedhenry", dockerHubRepo = name, credentialId = "dockerhubjenkins") {
    def version = sh(returnStdout: true, script: "node -p -e \"require('./package.json').version\"").trim().split('-')[0]
    sh "cp ./dist/fh-*x64.tar.gz docker/"
    dockerBinaryBuild(name, version, dockerHubOrg, dockerHubRepo, credentialId, './docker')
}
