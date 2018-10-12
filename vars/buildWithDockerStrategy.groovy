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

        def buildSelector = openshift.startBuild "${buildConfigName} --from-dir=${fromDir}"

        if (follow) {
            List<String> donePhases = ["COMPLETE", "FAILED", "ERROR", "CANCELLED"]
            timeout(30) {
                buildSelector.untilEach(1) { build ->
                    echo "Image Build Phase: ${build?.object()?.status?.phase}"
                    return (donePhases.contains(build?.object()?.status?.phase?.toUpperCase()))
                }
                try {
                    buildSelector.logs()
                } catch(e) {
                    echo "Couldn't stream the build logs"
                }

                assert buildSelector?.object()?.status?.phase?.toUpperCase() == "COMPLETE"
            }
        }
    }
}
