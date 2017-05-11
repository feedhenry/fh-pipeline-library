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

    archiveArtifacts "dist/${name}*.tar.gz"
}
