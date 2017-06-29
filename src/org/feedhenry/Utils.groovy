#!/usr/bin/groovy
package org.feedhenry

import java.text.SimpleDateFormat

def getReleaseBranch(version) {
    "RH_v${version}"
}

def getReleaseTag(version) {
    "rh-release-${version}-rc1"
}

def getBuildInfoFileName() {
    'build-info.json'
}

def mapToList(depmap) {
    def dlist = []
    for (entry in depmap) {
        dlist.add([entry.key, entry.value])
    }
    dlist
}

def mapToOptionsString(map) {
    def optionsArray = []
    for (def o in mapToList(map)) {
        optionsArray << "${o[0]}:${o[1]}"
    }
    optionsArray.join(" ")
}

def getArtifactsDir(name) {
    return "${name}-artifacts"
}

def gitRepoIsDirty(untrackedFiles='no') {
    return sh(returnStdout: true, script: "git status --porcelain --untracked-files=${untrackedFiles}").trim()
}

static Closure getDate() {
    Date now = new Date()
    SimpleDateFormat yearMonthDateHourMin = new SimpleDateFormat("yyyyMMddHHmm")
    return yearMonthDateHourMin.format(now)
}
