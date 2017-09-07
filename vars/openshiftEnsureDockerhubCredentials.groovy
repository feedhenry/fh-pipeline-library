#!/usr/bin/groovy

def call(String dockerHubOrg, String credentialId) {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: credentialId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        createOpenshiftDockerSecret "docker.io/${dockerHubOrg}", "${env.USERNAME}", "${env.PASSWORD}", "fh.team.eng-group@redhat.com", "${dockerHubOrg}"
    }
}
