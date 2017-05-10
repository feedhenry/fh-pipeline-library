#!/usr/bin/groovy

def call(Map parameters = [:], body) {

    def label = parameters.get('label', 'nodejs-ubuntu')

    node(label) {
        step([$class: 'WsCleanup'])

        stage ('Checkout') {
            checkout scm
        }

        //Use short git commit hash value for component build number
        env.BUILD_NUMBER = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()

        ansiColor('xterm') {
            body()
        }
    }
}
