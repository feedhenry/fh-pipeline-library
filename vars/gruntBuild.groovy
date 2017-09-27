#!/usr/bin/groovy

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def name = config.name
    def distCmd = config.distCmd ?: 'fh:dist'

    gruntCmd {
        cmd = distCmd
    }

    def pkgVersion = getBaseVersionFromPackageJson()
    def buildInfoFileName = writeBuildInfo(name, "${pkgVersion}-${env.BUILD_NUMBER}")

    archiveArtifacts "dist/${name}*.tar.gz, ${buildInfoFileName}"
}
