#!/usr/bin/groovy

import groovy.json.JsonSlurperClassic

def call(String jsonDefinition) {
    String kind = new JsonSlurperClassic().parseText(jsonDefinition).kind
    String definition = jsonDefinition
    openshift.withCluster() {
        try {
            openshift.create definition
        } catch (Exception e) {
            if (e.getMessage().contains("already exists")) {
                println "${kind} already exists, updating"
                try {
                    openshift.replace definition
                } catch (Exception e2) {
                    error "${kind} update failed with : ${e2}"
                }
            } else {
                error "${kind} creation failed with something other than 'already exists': ${e}"
            }
        }
    }
}
