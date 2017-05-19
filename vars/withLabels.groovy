#!/usr/bin/groovy

def call(labelSet) {
    labels = ((labelSet as Set) - [null]).join('&&')
    print "Requesting node with ${labels}"
    labels
}