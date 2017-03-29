#!/usr/bin/groovy
package org.feedhenry

def getReleaseBranch(version) {
    "RH_v${version}"
}

def getReleaseTag(version) {
    "rh-release-${version}-rc1"
}

def mapToList(depmap) {
    def dlist = []
    for (entry in depmap) {
        dlist.add([entry.key, entry.value])
    }
    dlist
}

def checkoutGitRepo(repoUrl, branch, targetDir = '.') {
    checkout([$class: 'GitSCM',
            branches: [[name: branch]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                    [$class: 'RelativeTargetDirectory',
                            relativeTargetDir: targetDir]
            ],
            submoduleCfg: [],
            userRemoteConfigs: [
                    [
                            credentialsId: 'jenkinsgithub',
                            url: repoUrl
                    ]
            ]
    ])
}

def getArtifactsDir(name) {
    return "${name}-artifacts"
}
