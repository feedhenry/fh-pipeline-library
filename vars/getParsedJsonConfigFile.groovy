#!/usr/bin/groovy

import groovy.json.JsonSlurperClassic

def call(uuid) {
    def text = org.jenkinsci.plugins.configfiles.GlobalConfigFiles.get().getById(uuid).content
    def result = new JsonSlurperClassic().parseText(text)
    return result
}
