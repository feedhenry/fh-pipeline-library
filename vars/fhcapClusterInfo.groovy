#!/usr/bin/groovy

import groovy.json.JsonSlurperClassic

def call(name) {
    def clusterInfoRaw = sh(returnStdout: true, script: "fhcap cluster info --name ${name} --only meta --format json").trim()
    return new JsonSlurperClassic().parseText(clusterInfoRaw)
}
