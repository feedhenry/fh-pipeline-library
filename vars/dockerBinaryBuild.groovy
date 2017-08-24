#!/usr/bin/groovy

def call(String name, String dockerhubOrg, String dockerHubRepo, String credentialId, String pkgVersion,Closure body) {
  openshiftEnsureDockerhubCredentials(dockerhubOrg, credentialId)
  
  def pushConfig = [
    "kind": "DockerImage",
    "name": "docker.io/${dockerhubOrg}/${dockerHubRepo}:${pkgVersion}-${env.BUILD_NUMBER}"
  ]
  
  openshiftEnsureBinaryBuild("${name}-bc", pushConfig)
  body()
}