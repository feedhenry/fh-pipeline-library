#!/usr/bin/groovy

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    sh '''
        npm install --production
        npm ls
        npm install -g https://github.com/bucharest-gold/license-reporter#720d739a94ae4190cbc8c078f47f018b8fd36d90
        license-reporter --ignore-version-range --all --silent --file licenses.xml
        npm install
        npm install grunt-cli -g
      '''
}
