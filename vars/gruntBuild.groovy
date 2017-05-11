#!/usr/bin/groovy

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def name = config.name
    def cmd = config.cmd ?: 'fh:dist --only-bundle-deps'

    sh "npm install grunt-cli -g"
    sh "grunt ${cmd}"

    archiveArtifacts "dist/${name}*.tar.gz"
}
