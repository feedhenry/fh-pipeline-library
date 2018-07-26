#!/usr/bin/groovy

def call(body = {}) {
    def config = [:]

    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def kubeSAPath = config.kubeSAPath ?: "/var/run/secrets/kubernetes.io/serviceaccount"
    def kubeUrl = config.kubeUrl ?: "kubernetes.default.svc.cluster.local"

    def ocStatus = -1;
    if (fileExists("${kubeSAPath}/token")) {
        def token = readFile("${kubeSAPath}/token")
        ocStatus = sh(script: "set +x;oc login ${kubeUrl} --certificate-authority=${kubeSAPath}/ca.crt --token=${token} &> /dev/null", returnStatus: true, returnStdout: false)
    }
    return ocStatus
}