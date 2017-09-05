#!/usr/bin/groovy

def call(name, dockerhubOrg="feedhenry", dockerHubRepo=name, credentialId="dockerhubjenkins") {
  def pkgVersion = sh(returnStdout: true, script: "node -p -e \"require('./package.json').version\"").trim().split('-')[0]
  dockerBinaryBuild(name,dockerhubOrg,dockerHubRepo,credentialId,pkgVersion) {
    sh "cp ./dist/fh-*x64.tar.gz docker/"
    sh "oc start-build ${name}-bc --from-dir ./docker" 
  }
}