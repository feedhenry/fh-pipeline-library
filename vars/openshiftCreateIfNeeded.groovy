import groovy.json.JsonSlurperClassic

def call(String jsonDefinition) {
    String kind = new JsonSlurperClassic().parseText(jsonDefinition).kind
    String definition = jsonDefinition
    openshift.withCluster() {
        try {
            openshift.create definition
        } catch (Exception e) {
            if (e.getMessage().contains("already exists")) {
                println "${kind} already exists"
            } else {
                error "${kind} creation failed with something other than 'already exists': ${e}"
            }
        }
    }
}
