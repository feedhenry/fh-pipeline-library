#!/usr/bin/groovy

def call(String dockerhubOrg, String credentialId) {
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: credentialId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
    createOpenshiftDockerSecret "docker.io/${dockerhubOrg}", "${env.USERNAME}", "${env.PASSWORD}", "fh.team.eng-group@redhat.com","${dockerhubOrg}"
  }
}