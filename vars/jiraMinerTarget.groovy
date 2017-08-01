def call(target="https://issues.jboss.org",credentialsId='fhautomatron-jira') {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: credentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        sh "jira-miner target ${target} --user \$USERNAME --password \$PASSWORD"
    }
}