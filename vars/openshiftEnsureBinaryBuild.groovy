import groovy.json.JsonOutput

def call (name, to, dockerSecret) {
  def pushingBuildTemplate = """{
    "apiVersion": "v1",
    "kind": "BuildConfig",
    "metadata": {
        "name": "${name}"
    },
    "spec": {
        "successfulBuildsHistoryLimit": 1,
        "failedBuildsHistoryLimit": 1,
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
                "dockerfilePath": ".",
                "pullSecret": {
                    "name": "${dockerSecret}"
                }
            }
        }
    }
  }"""

  openshiftCreateOrUpdate(pushingBuildTemplate)
}
