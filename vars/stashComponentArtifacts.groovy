#!/usr/bin/groovy
import org.feedhenry.Utils

def call(name, sha1, projectName) {
    node {
        step([$class: 'WsCleanup'])
        def utils = new Utils()

        step([$class: 'CopyArtifact',
                flatten: true,
                fingerprintArtifacts: true,
                parameters: "name=${name},sha1=${sha1}",
                projectName: projectName,
                selector: [
                        $class: 'StatusBuildSelector', stable: false],
                target: utils.getArtifactsDir(name)
        ])
        dir(utils.getArtifactsDir(name)) {
            writeFile file: 'sha1.txt', text: sha1
        }
        stash includes: "${utils.getArtifactsDir(name)}/", name: name
    }
}
