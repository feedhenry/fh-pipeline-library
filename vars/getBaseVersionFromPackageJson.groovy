#!/usr/bin/groovy

import groovy.json.JsonSlurperClassic

def call() {
    final String packageJson = readFile 'package.json'
    return new JsonSlurperClassic().parseText(packageJson).version.split('-')[0]
}
