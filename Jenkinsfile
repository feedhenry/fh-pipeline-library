#!/usr/bin/groovy

import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHUser
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder

final boolean isOrgMember(String user, String org, String gitHubCredentialsId) {
    node {
        withCredentials([usernamePassword(
                    credentialsId: gitHubCredentialsId,
                    passwordVariable: 'GITHUB_PASSWORD',
                    usernameVariable: 'GITHUB_USERNAME')]) {
            final GitHub gitHub = new GitHubBuilder()
                .withOAuthToken(env.GITHUB_PASSWORD, env.GITHUB_USERNAME)
                .build()

            final GHUser ghUser = gitHub.getUser(user)
            final GHOrganization ghOrganization = gitHub.getOrganization(org)
            return ghUser.isMemberOf(ghOrganization)
        }
    }
}

stage('Trust') {
    /*
     * This is copied from 'enforceTrustedApproval',
     * because using that here would not be trustworthy
     */

    if (!env.CHANGE_AUTHOR) {
        println "This doesn't look like a GitHub PR, continuing"
    } else if (!isOrgMember(env.CHANGE_AUTHOR, 'fheng', 'githubjenkins')) {
        input(
            message: "Trusted approval needed for change from ${env.CHANGE_AUTHOR}",
            submitter: 'authenticated'
        )
    } else {
        println "${env.CHANGE_AUTHOR} is trusted, continuing"
    }
}

def getTestComponentName() {
    return 'fh-mbaas'
}

def getTestComponentConfigs() {
    def componentConfigs = [:]
    def componentName = getTestComponentName()
    componentConfigs[componentName] = [:]
    componentConfigs[componentName]['gitHubOrg'] = 'feedhenry'
    componentConfigs[componentName]['repoName'] = 'fh-mbaas'
    componentConfigs[componentName]['baseBranch'] = 'master'
    componentConfigs[componentName]['repoDir'] = ''
    componentConfigs[componentName]['cookbook'] = 'fh-mbaas'
    componentConfigs[componentName]['buildType'] = 'node'
    componentConfigs[componentName]['distCmd'] = 'grunt fh:dist'
    componentConfigs[componentName]['buildJobName'] = "build_any_jenkinsfile"
    componentConfigs[componentName]['gitUrl'] = "git@github.com:feedhenry/fh-mbaas.git"
    componentConfigs[componentName]['gitHubUrl'] = "https://github.com/feehdenry/fh-mbaas"
    return componentConfigs
}

def testStage(name, body) {
    stage(name) {
        step([$class: 'WsCleanup'])
        print "#### test_${name} ####"
        body()
    }
}

node {
    def fhPipelineLibrary = library("fh-pipeline-library@${env.BRANCH_NAME}")
    def utils = fhPipelineLibrary.org.feedhenry.Utils.new()

    testStage('getReleaseBranch') {
        print utils.getReleaseBranch('1.2.3')
    }

    testStage('getReleaseTag') {
        print utils.getReleaseTag('1.2.3')
    }

    testStage('getArtifactsDir') {
        print utils.getArtifactsDir('fh-ngui')
    }

    testStage('checkoutGitRepo') {
        checkoutGitRepo {
            repoUrl = 'git@github.com:feedhenry/fh-pipeline-library.git'
            branch = 'master'
        }
        sh('ls')
    }

    testStage('eachComponent') {
        def allComponents = [getTestComponentName()]
        def componentConfigs = getTestComponentConfigs()

        eachComponent(allComponents, componentConfigs) { name, gitHubOrg, gitUrl, gitHubUrl, config ->
            print name
            print gitHubOrg
            print gitUrl
            print gitHubUrl
            print config
        }
    }

    testStage('getComponentConfigs') {
        def configGitRepo = "git@github.com:feedhenry/product_releases.git"
        def configGitRef = 'master'

        def componentConfigs = getComponentConfigs(configGitRepo, configGitRef)

        print componentConfigs
    }

    testStage('getTemplateAppComponentConfigs') {
        def configGitRepo = "git@github.com:feedhenry/fh-template-apps.git"
        def configGitRef = 'master'

        def componentConfigs = getTemplateAppComponentConfigs(configGitRepo, configGitRef)

        print componentConfigs
    }

    testStage('fhcapNode') {
        fhcapNode(gitRepo: 'git@github.com:fheng/fhcap-cli.git', gitRef: 'master') {
            print env.PATH
            sh "fhcap --version"
            sh "fhcap info"
            sh "openssl version"
            sh "nova --version 2>&1 | grep 7.1.0"
            sh "aws --version 2>&1 | grep 1.11.80"
            sh "date"
        }
    }

    testStage('Koji') {
        String kojiUrl = "https://koji.fedoraproject.org/kojihub"
        def koji = fhPipelineLibrary.org.feedhenry.Koji.new(kojiUrl)

        // If this starts failing in a few years, update with a new build from here:
        // https://koji.fedoraproject.org/koji/packageinfo?packageID=8
        String name    = "kernel"
        String version = "4.15.2"
        String release = "301.fc27"
        Map<String, String> aKernelBuild = koji.getBuild("${name}-${version}-${release}")

        println aKernelBuild

        assert aKernelBuild.name    == name
        assert aKernelBuild.version == version
        assert aKernelBuild.release == release

        assert koji.listArchives(aKernelBuild.build_id) == []
    }
}
