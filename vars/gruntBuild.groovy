#!/usr/bin/groovy

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def name = config.name
    def distCmd = config.distCmd ?: 'fh:dist --only-bundle-deps'

    gruntCmd {
        cmd = distCmd
    }

    def pkgVersion = sh(returnStdout: true, script: "node -p -e \"require('./package.json').version\"").trim()
    def buildInfoFileName = writeBuildInfo(name, "${pkgVersion}-${env.BUILD_NUMBER}")

    archiveArtifacts "dist/${name}*.tar.gz, ${buildInfoFileName}"
}
