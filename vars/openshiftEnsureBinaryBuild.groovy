import groovy.json.JsonOutput

def call (name, to) {
  def pushingBuildTemplate = """{
    "apiVersion": "v1",
    "kind": "BuildConfig",
    "metadata": {
        "name": "${name}"
    },
    "spec": {
        "output": {
          "to": ${JsonOutput.toJson(to)}
        },
        "runPolicy": "Serial",
        "source": {
            "type": "Binary",
            "binary": {}
        },
        "strategy": {
            "type": "Docker",
            "dockerStrategy": {
                "dockerfilePath": "."
            }
        }
    }
  }"""

  openshiftCreateOrUpdate(pushingBuildTemplate)
}
