Closure call(String username, String password, String email) {
    openshift.withCluster() {

        if (openshift.selector('secrets').names().contains('secret/dockerhub')) {
            openshift.selector('secret/dockerhub').delete()
        }

        openshift.secrets(
            "new-dockercfg",
            "dockerhub",
            "--docker-server=docker.io/fhwendy",
            "--docker-username=${username}",
            "--docker-password=${password}",
            "--docker-email=${email}"
        )

        openshift.secrets('link', 'builder', 'dockerhub')
    }
}
