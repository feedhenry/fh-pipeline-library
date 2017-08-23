#!/usr/bin/groovy

import org.feedhenry.Utils

def call(configGitRepo, configGitRef, tryMasterOnFail = false) {
    def componentConfigs = [:]
    node('nodejs') {
        step([$class: 'WsCleanup'])

        def utils = new Utils()

        try {
            checkoutGitRepo {
                repoUrl = configGitRepo
                branch = configGitRef
                shallow = true
            }
        } catch (Exception e) {
            if (tryMasterOnFail) {
                checkoutGitRepo {
                    repoUrl = configGitRepo
                    branch = 'master'
                    shallow = true
                }
            } else {
                throw e
            }
        }

        writeTemplateAppsConfigGruntTask()

        sh('npm install --ignore-scripts')
        sh('npm install grunt-cli -g')

        sh("grunt configs --file-name 'TEMPLATE_APP_COMPONENTS.json'")

        componentConfigs = utils.parseComponentsFile('TEMPLATE_APP_COMPONENTS.json', 'template-apps')
    }
    return componentConfigs
}