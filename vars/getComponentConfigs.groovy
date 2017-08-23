#!/usr/bin/groovy

import org.feedhenry.Utils

def call(configGitRepo, configGitRef, tryMasterOnFail = false) {
    def componentConfigs = [:]
    node {
        step([$class: 'WsCleanup'])

        def utils = new Utils()

        try {
            checkoutGitRepo {
                repoUrl = configGitRepo
                branch = configGitRef
                targetDir = 'product_releases'
                shallow = true
            }
        } catch (Exception e) {
            if (tryMasterOnFail) {
                checkoutGitRepo {
                    repoUrl = configGitRepo
                    branch = 'master'
                    targetDir = 'product_releases'
                    shallow = true
                }
            } else {
                throw e
            }
        }

        dir('product_releases') {
            componentConfigs = utils.parseComponentsFile('COMPONENTS.json', 'platform')
        }
    }
    return componentConfigs
}
