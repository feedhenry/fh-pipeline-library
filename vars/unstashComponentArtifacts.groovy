#!/usr/bin/groovy

import groovy.json.JsonSlurperClassic
import org.feedhenry.Utils

def call(name, body) {
    def utils = new Utils()
    unstash name
    dir(utils.getArtifactsDir(name)) {
        def buildInfoFileName = utils.getBuildInfoFileName()
        if (!fileExists(buildInfoFileName)) {
            error "${buildInfoFileName} does not exist in the artifacts of ${name}"
        }
        def buildInfoRaw = readFile buildInfoFileName
        buildInfo = new JsonSlurperClassic().parseText buildInfoRaw
        def version = buildInfo[name]['version']
        def build = buildInfo[name]['build']
        body(version, build)
    }
}
