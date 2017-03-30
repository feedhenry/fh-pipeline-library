#!/usr/bin/groovy
import org.feedhenry.Utils

def call(name, body) {
    def utils = new Utils()
    unstash name
    dir(utils.getArtifactsDir(name)) {
        def version = readFile "VERSION.txt"
        version = version.trim().split('-')[0]
        def build = readFile "sha1.txt"
        build = build.take(7)
        body(version, build)
    }
}
