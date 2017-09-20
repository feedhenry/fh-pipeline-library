#!/usr/bin/groovy

/*
 * To be able to use the method exposed here, the following script signatures
 * must be approved:
 *
 * method org.kohsuke.github.GHUser isMemberOf org.kohsuke.github.GHOrganization
 * method org.kohsuke.github.GitHub getOrganization java.lang.String
 * method org.kohsuke.github.GitHub getUser java.lang.String
 * method org.kohsuke.github.GitHubBuilder build
 * method org.kohsuke.github.GitHubBuilder withOAuthToken java.lang.String java.lang.String
 * new org.kohsuke.github.GitHubBuilder
 *
 */
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

def call(String trustedOrg='fheng', String gitHubCredentialsId='githubjenkins') {
    if (!env.CHANGE_AUTHOR) {
        println "This doesn't look like a GitHub PR, continuing"
    } else if (!isOrgMember(env.CHANGE_AUTHOR, trustedOrg, gitHubCredentialsId)) {
        input(
            message: "Trusted approval needed for change from ${env.CHANGE_AUTHOR}",
            submitter: 'authenticated'
        )
    } else {
        println "${env.CHANGE_AUTHOR} is trusted, continuing"
    }
}
