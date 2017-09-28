#!/usr/bin/groovy
package org.feedhenry

import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonSlurperClassic
import java.text.SimpleDateFormat

static def getReleaseBranch(version) {
    def versionParts = version.tokenize(".")
    return "FH-v${versionParts[0]}.${versionParts[1]}"
}

static def getReleaseTag(version, candidate=null) {
    if(candidate) {
        "release-${version}-${candidate}"
    } else {
        "release-${version}"
    }
}

static def getBuildInfoFileName() {
    'build-info.json'
}

static def mapToList(depmap) {
    def dlist = []
    for (entry in depmap) {
        dlist.add([entry.key, entry.value])
    }
    dlist
}

static def mapToOptionsString(map, delim=':') {
    def optionsArray = []
    for (def o in mapToList(map)) {
        optionsArray << (o - '' - null).join(delim)
    }
    optionsArray.join(" ")
}

static def getArtifactsDir(name) {
    return "${name}-artifacts"
}

def gitRepoIsDirty(untrackedFiles='no') {
    return sh(returnStdout: true, script: "git status --porcelain --untracked-files=${untrackedFiles}").trim()
}

static def getDate() {
    Date now = new Date()
    SimpleDateFormat yearMonthDateHourMin = new SimpleDateFormat("yyyyMMddHHmm")
    return yearMonthDateHourMin.format(now)
}

def parseComponentsFile(componentsFile, componentType) {
    def componentConfigs = [:]
    componentsJson = readFile componentsFile
    def object = new JsonSlurperClassic().parseText componentsJson
    def components = object['components']

    for (i = 0; i < components.size(); i++) {
        def index = i
        if (components[index]['type'] == componentType) {
            def config = components[index]
            def componentName = config['name']
            componentConfigs[componentName] = [:]
            componentConfigs[componentName]['gitHubOrg'] = params.gitHubOrg ?: config['org']
            componentConfigs[componentName]['repoName'] = config['repoName'] ?: componentName
            componentConfigs[componentName]['baseBranch'] = config['baseBranch'] ?: 'master'
            componentConfigs[componentName]['repoDir'] = config['repoDir'] ?: ''
            componentConfigs[componentName]['cookbook'] = config['cookbook'] ?: componentName
            componentConfigs[componentName]['buildType'] = config['buildType'] ?: 'node'
            componentConfigs[componentName]['distCmd'] = config['distCmd']
            componentConfigs[componentName]['labels'] = config?.labels ?: {}
            componentConfigs[componentName]['type'] = config['type']
            componentConfigs[componentName]['buildJobName'] = config['buildJobName'] ?: "build_any_jenkinsfile"
            componentConfigs[componentName]['gitUrl'] = "git@github.com:${componentConfigs[componentName]['gitHubOrg']}/${componentConfigs[componentName]['repoName']}.git"
            componentConfigs[componentName]['gitHubUrl'] = "https://github.com/${componentConfigs[componentName]['gitHubOrg']}/${componentConfigs[componentName]['repoName']}"
        }
    }
    return componentConfigs
}

@NonCPS
def filterRhmap4Components(components) {
    components.findAll({ k, v -> v?.labels?.rhmap4 == true })
}

@NonCPS
def filterRhmap4CoreComponents(components) {
    components.findAll({ k, v -> v?.labels?.rhmap4 == true && v?.labels?.core == true })
}

@NonCPS
def filterRhmap4MbaasComponents(components) {
    components.findAll({ k, v -> v?.labels?.rhmap4 == true && v?.labels?.mbaas == true })
}

@NonCPS
def filterRhmap3Components(components) {
    components.findAll({ k, v -> v?.labels?.rhmap3 == true })
}

@NonCPS
def filterTemplateAppComponents(components) {
    components.findAll({ k, v -> v?.type == "template-apps" })
}

@NonCPS
def filterPlatformComponents(components) {
    components.findAll({ k, v -> v?.type == "platform" })
}
