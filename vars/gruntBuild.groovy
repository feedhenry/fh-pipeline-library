#!/usr/bin/groovy

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def name = config.name
    def distCmd = config.distCmd ?: 'fh:dist'

    // TODO: install this from npmjs once it's published
    sh "npm install -g https://github.com/bucharest-gold/license-reporter"
    sh "license-reporter --ignore-version-range --all --silent --file licenses.xml"

    gruntCmd {
        cmd = distCmd
    }

    def pkgVersion = sh(returnStdout: true, script: "node -p -e \"require('./package.json').version\"").trim()
    def buildInfoFileName = writeBuildInfo(name, "${pkgVersion}-${env.BUILD_NUMBER}")

    archiveArtifacts "dist/${name}*.tar.gz, ${buildInfoFileName}"
}
