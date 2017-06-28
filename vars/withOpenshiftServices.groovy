def call(services, body) {
    List<String> names = getNames(services)
    try {
        createOpenshiftResources(services, names)
        withEnv(env(services, names)) {
            sleep 10
            body()
        }
    } finally {
        cleanup(names)
    }
}

def cleanup(names) {
    for (int i = 0; i < names.size(); i++) {
        String resourceName = names[0]
        openshiftScale deploymentConfig: resourceName,  replicaCount: 0
        openshift.withCluster() {
            openshift.selector('svc', [name: resourceName]).delete()
            openshift.selector('dc', [name: resourceName]).delete()
        }
    }
}

def createOpenshiftResources(services, names) {
    Map jobs = [:]
    for (int i = 0; i < services.size(); i++) {
        String service = services[i]
        String name = names[i]
        jobs[name] = {
            openshiftCreateResource(getDeploymentConfigYaml(service, name))
            openshiftCreateResource(getServiceYaml(service, name))
            openshiftScale deploymentConfig: name,  replicaCount: 1, verifyReplicaCount: 1, waitTime: 600000
        }
    }
    parallel jobs
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
