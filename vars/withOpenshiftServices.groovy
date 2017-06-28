def call(services, body) {
    def buildingClosure = { prevFn, _service, _name ->
        String name = _name
        String service = _service
        try {
            openshiftCreateResource(getDeploymentConfigYaml(service, name))
            openshiftCreateResource(getServiceYaml(service, name))
            openshiftScale deploymentConfig: name,  replicaCount: 1, verifyReplicaCount: 1
            prevFn()
        } finally {
            openshift.withCluster() {
                try {
                    openshiftScale deploymentConfig: name,  replicaCount: 0
                } finally {
                    try {
                        openshift.selector('svc', [name: name]).delete()
                    } finally {
                        openshift.selector('dc', [name: name]).delete()
                    }
                }
            }
        }
    }

    List<String> names = getNames(services)
    def builtFunction = body;
    for (int i = 0; i < names.size(); i++) {
        builtFunction = buildingClosure.curry(builtFunction, services[i], names[i])
    }

    withEnv(env(services, names)) {
        builtFunction()
    }
}

String sanitizeObjectName(s) {
    s.replace('_', '-')
            .toLowerCase()
            .reverse()
            .take(23)
            .replaceAll("^-+", "")
            .reverse()
            .replaceAll("^-+", "")
}

Map<String, String> getNames(services) {
    List<String> names = []
    for (int i = 0; i < services.size(); i++) {
        names += sanitizeObjectName("${env.BUILD_TAG}-${services[i]}")
    }
    return names
}

List<String> env(services, names) {
    List<String> out = []
    if (services.contains('mongodb')) {
        out.add("MONGODB_HOST=${names[services.indexOf('mongodb')]}")
    }
    return out
}

String getDeploymentConfigYaml(service, name) {
    switch (service) {
        case 'mongodb':
        return """
apiVersion: v1
kind: DeploymentConfig
metadata:
  name: ${name}
  labels:
    name: ${name}
spec:
  replicas: 0
  selector:
    name: ${name}
  strategy:
    recreateParams:
      timeoutSeconds: 600
    type: Recreate
  template:
    metadata:
      labels:
        name: ${name}
    spec:
      containers:
      - image: docker.io/mongo:3
        imagePullPolicy: IfNotPresent
        name: mongodb
        ports:
        - containerPort: 27017
          protocol: TCP
        volumeMounts:
        - mountPath: /data/db
          name: data
        readinessProbe:
          exec:
            command:
              - /bin/sh
              - '-ic'
              - echo 'db.stats().ok' | mongo 127.0.0.1:27017/admin
          timeoutSeconds: 5
          periodSeconds: 10
          successThreshold: 1
          failureThreshold: 3
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      volumes:
      - emptyDir: {}
        name: data
"""
        default:
        return ''
    }
}

String getServiceYaml(service, name) {
    switch (service) {
        case 'mongodb':
        return """
apiVersion: v1
kind: Service
metadata:
  name: ${name}
  labels:
    name: ${name}
spec:
  ports:
  - port: 27017
    protocol: TCP
    targetPort: 27017
  selector:
    name: ${name}
  type: ClusterIP
"""
        default:
        return ''
    }
}
