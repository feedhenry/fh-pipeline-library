def call(String dockerServer, String username, String password, String email, String secretName='dockerhub') {
    openshift.withCluster() {

        // OCP 3.6 changed from secret/ to secrets/
        List<String> candidates = ["secret/${secretName}", "secrets/${secretName}"]
        List<String> secrets = openshift.selector('secrets').names()
        List<String> matches = secrets.intersect(candidates)

        if (!matches.empty) {
            for (int i=0; i < matches.size; i++)
            openshift.selector(matches[i]).delete()
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
