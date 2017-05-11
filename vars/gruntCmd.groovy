#!/usr/bin/groovy

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def cmd = config.cmd

    sh "npm install grunt-cli -g"
    sh "grunt ${cmd}"
}
