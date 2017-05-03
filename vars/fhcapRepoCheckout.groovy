#!/usr/bin/groovy

def call(name, gitRef) {
    sh "fhcap repo checkout --name ${name} --git-ref ${gitRef}"
}
