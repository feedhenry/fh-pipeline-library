#!/usr/bin/groovy

def call(Map parameters = [:], body) {

    def labels = [parameters.get('label','ruby-fhcap')]
    labels += parameters.get('labels',[]) 
    def provider = parameters.get('provider')  ?: [:]
    labels += provider.get('labels',[])

    node(withLabels(labels)) {

        def installLatest = parameters.get('installLatest', false)
        def credentialsId = parameters.get('credentialsId', 'jenkinsgithub')
        def configFile = parameters.get('configFile', "${WORKSPACE}/fhcap.json")
        def gitRepo = parameters.get('gitRepo', null)
        def gitRef = parameters.get('gitRef', 'master')
        def gitUserName = parameters.get('gitUserName', null)
        def gitUserEmail = parameters.get('gitUserEmail', null)
        def fhcapRepos = parameters.get('fhcapRepos', null)

        step([$class: 'WsCleanup'])
        sshagent([credentialsId]) {

            sh "rm -rf ${HOME}/fhcap"

            if (gitUserName && gitUserEmail) {
                sh "git config --global user.name \"${gitUserName}\""
                sh "git config --global user.email \"${gitUserEmail}\""
            }

            ansiColor('xterm') {
                env.RUBYOPT = "-W0"

                if (gitRepo) {
                    dir('fhcap-cli') {
                        checkoutGitRepo {
                            repoUrl = gitRepo
                            branch = gitRef
                        }
                        sh "rake install"
                    }
                } else {
                    if (installLatest) {
                        sh "gem install fhcap-cli --no-ri --no-rdoc"
                    }
                }
                sh "fhcap --version"

                env.PATH = "${PATH}:/home/jenkins/bin"
                env.FHCAP_CFG_FILE = configFile

                sh "yes | fhcap setup --repos-dir ${WORKSPACE} --fh-src-dir ${WORKSPACE}"

                if (fhcapRepos) {
                    fhcapRepoAddBatch {
                        repos = fhcapRepos
                    }
                }

                body()
            }
        }
    }
}
