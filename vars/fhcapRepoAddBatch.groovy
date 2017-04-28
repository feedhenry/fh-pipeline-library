#!/usr/bin/groovy
import org.feedhenry.Utils

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def utils = new Utils()

    def repos = config.repos ?: [:]

    for (def repo in utils.mapToList(repos)) {
        fhcapRepoAdd {
            name = repo[0]
            url = repo[1]
        }
    }
}
