#!/usr/bin/groovy

import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic
import org.feedhenry.Utils

def call(name, versionTxt) {
    def utils = new Utils()
    def buildInfoFileName = utils.getBuildInfoFileName()
    def buildInfo = [:]

    if(fileExists(buildInfoFileName)) {
        def buildInfoRaw = readFile buildInfoFileName
        print buildInfoRaw
        buildInfo = new JsonSlurperClassic().parseText buildInfoRaw
        print buildInfo
    }

    buildInfo['jenkinsUrl'] = env.JENKINS_URL
    buildInfo['buildUrl'] = env.BUILD_URL
    buildInfo['sha1'] = sh(returnStdout: true, script: 'git log -n 1 --pretty=format:"%H"').trim()

    buildInfo[name] = [:]
    buildInfo[name]['version'] = versionTxt.split('-')[0]
    buildInfo[name]['build'] = env.BUILD_NUMBER

    writeFile file: buildInfoFileName, text: new JsonBuilder(buildInfo).toPrettyString()
    return buildInfoFileName
}
