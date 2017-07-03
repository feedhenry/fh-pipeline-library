def call(String dockerServer, String username, String password, String email, String secretName='dockerhub') {
    openshift.withCluster() {

        if (openshift.selector('secrets').names().contains("secret/${secretName}" as String)) {
            openshift.selector("secret/${secretName}").delete()
        }

        openshift.secrets(
            "new-dockercfg",
            secretName,
            "--docker-server=${dockerServer}",
            "--docker-username=${username}",
            "--docker-password=${password}",
            "--docker-email=${email}"
        )

        openshift.secrets('link', 'builder', secretName)
    }
}
