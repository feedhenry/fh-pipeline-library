#!/usr/bin/groovy
package org.feedhenry

import java.text.SimpleDateFormat

static def getReleaseBranch(version) {
    def versionParts = version.tokenize(".")
    return "FH-v${versionParts[0]}.${versionParts[1]}"
}

static def getReleaseTag(version, candidate=null) {
    if (candidate) {
        return "release-${version}-${candidate}"
    } else {
        return "release-${version}"
    }
}

static def getBuildInfoFileName() {
    return 'build-info.json'
}

static def mapToList(depmap) {
    def dlist = []
    for (entry in depmap) {
        dlist.add([entry.key, entry.value])
    }
    return dlist
}

static def mapToOptionsString(map) {
    def optionsArray = []
    for (def o in mapToList(map)) {
        optionsArray << "${o[0]}:${o[1]}"
    }
    return optionsArray.join(" ")
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

static String sanitizeK8sObjectName(String s, int maxLength=63) {
    return s.replace('_', '-')
        .replace('.', '-')
        .replace('/', '-')
        .toLowerCase()
        .reverse()
        .take(maxLength)
        .replaceAll("^-+", "")
        .reverse()
        .replaceAll("^-+", "")
}
