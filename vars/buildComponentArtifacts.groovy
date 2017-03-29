#!/usr/bin/groovy

def call(name, sha1, projectName, config) {
    buildComponent(name, sha1, projectName, config)
    stashComponentArtifacts(name, sha1, projectName)
}
