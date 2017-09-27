#!/usr/bin/groovy

def call(Map params) {

    final String outputImage = params.outputImage
    final String buildConfigName = params.buildConfigName

    final Boolean follow = params.follow ?: true
    final String fromDir = params.fromDir ?: "./docker"
    final String imageRepoSecret = params.imageRepoSecret ?: "dockerhub"

    final String buildConfig = """
    {
        "kind": "BuildConfig",
        "apiVersion": "v1",
        "metadata": {
            "name": "${buildConfigName}"
        },
        "spec": {
            "successfulBuildsHistoryLimit": 1,
            "failedBuildsHistoryLimit": 1,
            "runPolicy": "Serial",
            "source": {
                "type": "Binary",
                "binary": {}
            },
            "strategy": {
                "type": "Docker",
                "dockerStrategy": {
                    "pullSecret": {
                        "name": "${imageRepoSecret}"
                    }
                }
            },
            "output": {
                "to": {
                    "kind": "DockerImage",
                    "name": "${outputImage}"
                }
            }
        }
    }
    """

    openshift.withCluster() {
        openshift.apply buildConfig
        sh "oc start-build ${buildConfigName} --follow=${follow} --from-dir=${fromDir}"
    }
}
