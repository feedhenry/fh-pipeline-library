#!/usr/bin/groovy

import org.feedhenry.Utils

def call(name, sha1, projectName) {
    node {
        step([$class: 'WsCleanup'])
        def utils = new Utils()

        step([$class: 'CopyArtifact',
                flatten: true,
                fingerprintArtifacts: true,
                parameters: "componentName=${name},sha1=${sha1}",
                projectName: projectName,
                selector: [
                        $class: 'StatusBuildSelector', stable: true],
                target: utils.getArtifactsDir(name)
        ])
        dir(utils.getArtifactsDir(name)) {
            def buildInfoFileName = utils.getBuildInfoFileName()
            if (!fileExists(buildInfoFileName)) {
                error "${buildInfoFileName} does not exist in the artifacts of ${name}"
            }
        }
        stash includes: "${utils.getArtifactsDir(name)}/", name: name
    }
}
