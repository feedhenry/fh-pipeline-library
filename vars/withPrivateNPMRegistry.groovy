def call(String credentialsId = 'sinopiajenkins', body) {
    withCredentials([string(
                credentialsId: credentialsId,
                variable: 'PRIVATE_NPM_AUTH')]) {
        sh "printf ${env.PRIVATE_NPM_AUTH} > .npmrc"
        body()
        sh 'rm .npmrc'
    }
}
