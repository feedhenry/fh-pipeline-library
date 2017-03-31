#!/usr/bin/groovy

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def name = config.name
    def url = config.url
    def clustersDir = config.clustersDir ?: 'clusters'
    def verbose = config.verbose ?: false
    def credentialsId = config.credentialsid ?: 'jenkinsgithub'

    sshagent([credentialsId]) {
        sh "fhcap repo add --name ${name} --url ${url} --clusters-dir ${clustersDir} --verbose ${verbose}"
    }
}
