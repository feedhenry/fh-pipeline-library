#!/usr/bin/groovy

import org.feedhenry.Utils

def checkoutFhcapCli(gitRepo = 'git@github.com:fheng/fhcap-cli.git', gitRef = 'master', install = false) {
    dir('fhcap-cli') {
        checkoutGitRepo {
            repoUrl = gitRepo
            branch = gitRef
            shallow = true
            noTags = true
        }
        if (install) {
            sh "rake install"
        }
    }
}

def call(Map parameters = [:], body) {

    def labels = [parameters.get('label', 'ruby-fhcap')]
    labels += parameters.get('labels', [])
    def provider = parameters.get('provider') ?: [:]
    labels += provider.get('labels', [])

    node(withLabels(labels)) {

        def configFile = parameters.get('configFile', "${env.WORKSPACE}/fhcap.json")
        withEnv(["PATH+=/home/jenkins/bin", "RUBYOPT=-W0", "FHCAP_CFG_FILE=${configFile}"]) {
            def utils = new Utils()
            def installLatest = parameters.get('installLatest', false)
            def credentialsId = parameters.get('credentialsId', 'jenkinsgithub')
            def gitRepo = parameters.get('gitRepo', null)
            def gitRef = parameters.get('gitRef', 'master')
            def gitUserName = parameters.get('gitUserName', null)
            def gitUserEmail = parameters.get('gitUserEmail', null)
            def fhcapRepos = parameters.get('fhcapRepos', null)
            def workDir = parameters.get('workDir', '.')
            def setupOpts = parameters.get('setupOpts', [:])
            def defaultSetupOts = ['--repos-dir': WORKSPACE, '--fh-src-dir': WORKSPACE, '--knife-dir': WORKSPACE]

            String setupOptsStr = utils.mapToOptionsString(defaultSetupOts + setupOpts, ' ')

            step([$class: 'WsCleanup'])
            sshagent([credentialsId]) {

                sh "rm -rf ${HOME}/fhcap"

                if (gitUserName && gitUserEmail) {
                    sh "git config --global user.name \"${gitUserName}\""
                    sh "git config --global user.email \"${gitUserEmail}\""
                }

                ansiColor('xterm') {
                    if (gitRepo) {
                        checkoutFhcapCli(gitRepo, gitRef, true)
                    } else {
                        //This is only here to get git clones to work correctly, and avoid 'Host key verification failed' errors
                        checkoutFhcapCli()
                        if (installLatest) {
                            sh "gem install fhcap-cli --no-ri --no-rdoc"
                        }
                    }
                    sh "fhcap --version"
                    sh "yes | fhcap setup ${setupOptsStr}"

                    if (fhcapRepos) {
                        fhcapRepoAddBatch {
                            repos = fhcapRepos
                        }
                    }

                    dir(workDir) {
                        body()
                    }
                }
            }
        }
    }
}
