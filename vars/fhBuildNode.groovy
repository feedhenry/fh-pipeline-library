#!/usr/bin/groovy

def call(Map parameters = [:], body) {
    // We plan to deprecate the 'label' entry in params and use 'labels', containing a list of required labels
    // The logic is:
    // if labels exist, use those
    // if label exists, use that
    // if nothing exist use nodejs6
    // in the future, we will replace this with:
    // def labels = parameters.get('labels',[])
    def labels = parameters.get('labels',[parameters.get('label','nodejs6')])

    node(withLabels(labels)) {
        step([$class: 'WsCleanup'])

        if(params.componentName) {
            currentBuild.displayName = "${currentBuild.displayName} ${params.componentName}"
        }

        stage ('Checkout') {
            checkout scm
        }

        if(params.componentName) {
            def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
            currentBuild.description = gitCommit
        }

        //Use short git commit hash value for component build number
        env.BUILD_NUMBER = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()

        ansiColor('xterm') {
            body()
        }
    }
}
