#!/usr/bin/groovy

import org.feedhenry.Utils

def call(configGitRepo, configGitRef, tryMasterOnFail = false) {
    def componentConfigs = [:]
    node('nodejs6') {
        step([$class: 'WsCleanup'])

        def utils = new Utils()
        def backupRef = false;

        if(tryMasterOnFail) {
           backupRef = 'master'
        }

        checkoutGitRepo {
            repoUrl = configGitRepo
            branch = configGitRef
            shallow = true
            backupBranch = backupRef
        }

        writeTemplateAppsConfigGruntTask()

        sh('npm install --ignore-scripts')
        sh('npm install grunt-cli -g')

        sh("grunt configs --file-name 'TEMPLATE_APP_COMPONENTS.json'")

        componentConfigs = utils.parseComponentsFile('TEMPLATE_APP_COMPONENTS.json', 'template-apps')
    }
    return componentConfigs
}
