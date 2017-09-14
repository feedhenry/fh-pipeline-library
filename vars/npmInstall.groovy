#!/usr/bin/groovy

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    sh '''
        npm install --production
        npm ls
        npm install -g https://github.com/bucharest-gold/license-reporter#v0.3.0
        license-reporter --ignore-version-range --all --silent --html --file license.html
        npm install
        npm install grunt-cli -g
      '''
}
