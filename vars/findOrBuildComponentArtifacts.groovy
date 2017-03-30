#!/usr/bin/groovy

def call(name, sha1, projectName, config) {
    try {
        stashComponentArtifacts(name, sha1, projectName)
    } catch (Exception e1) {
        buildComponentArtifacts(name, sha1, projectName, config)
    }
}
