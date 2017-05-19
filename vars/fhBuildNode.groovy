#!/usr/bin/groovy

def call(Map parameters = [:], body) {
    // We plan to deprecate the 'label' entry in params and use 'labels', containing a list of required labels
    // The logic is:
    // if labels exist, use those
    // if label exists, use that
    // if nothing exitst use nodejs-ubuntu
    // in the future, we will replace this with:
    // def labels = parameters.get('labels',[])
    def labels = parameters.get('labels',[parameters.get('label','nodejs-ubuntu')])

    node(withLabels(labels)) {
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
