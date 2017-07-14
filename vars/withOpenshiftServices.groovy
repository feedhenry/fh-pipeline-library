def call(List<String> services, Closure body) {
    List<String> names = getNames(services)
    try {
        createOpenshiftResources(services, names)
        withEnv(env(services, names)) {
            body()
        }
    } finally {
        cleanup(names)
    }
}

def cleanup(List<String> names) {
    for (int i = 0; i < names.size(); i++) {
        String resourceName = names[0]

        try {
            retry (3) {
                echo "[withOpenshiftServices] Attempting to scale down ${resourceName}"
                openshiftScale deploymentConfig: resourceName,  replicaCount: 0
            }
        } catch (e) {
            echo "[withOpenshiftServices] Failed to scale down ${resourceName} after 3 attempts"
        }

        try {
            retry (3) {
                echo "[withOpenshiftServices] Attempting to delete DC ${resourceName}"
                openshift.withCluster() {
                    openshift.selector('svc', [name: resourceName]).delete()
                }
            }
        } catch (e) {
            echo "[withOpenshiftServices] Failed to delete DC ${resourceName} after 3 attempts"
        }

        try {
            retry (3) {
                echo "[withOpenshiftServices] Attempting to delete SVC ${resourceName}"
                openshift.withCluster() {
                    openshift.selector('dc', [name: resourceName]).delete()
                }
            }
        } catch (e) {
            echo "[withOpenshiftServices] Failed to delete SVC ${resourceName} after 3 attempts"
        }
    }
}

def createOpenshiftResources(List<String> services, List<String> names) {
    Map<String, Closure> jobs = [:]
    for (int i = 0; i < services.size(); i++) {
        String service = services[i]
        String name = names[i]
        jobs[name] = {
            openshiftCreateResource(getDeploymentConfigYaml(service, name))
            openshiftCreateResource(getServiceYaml(service, name))
            openshiftScale deploymentConfig: name,  replicaCount: 1, verifyReplicaCount: 1, waitTime: 600000
            waitForServiceToBeReady service, name
        }
    }
    parallel jobs
}

def waitForServiceToBeReady(String service, String name) {
    if (service == 'mongodb') {
        writeFile(
            file: 'checkMongo.sh',
            text: "echo 'daves\ndaves()\n/' | curl -v telnet://${name}:27017/"
        )
        sh 'chmod +x checkMongo.sh'
        timeout(5) {
            waitUntil {
                def xit = sh script: "./checkMongo.sh", returnStatus: true
                return xit == 0
            }
        }
        sh 'rm checkMongo.sh'
    }
}

String sanitizeObjectName(String s) {
    s.replace('_', '-')
        .replace('.', '-')
        .toLowerCase()
        .reverse()
        .take(23)
        .replaceAll("^-+", "")
        .reverse()
        .replaceAll("^-+", "")
}

Map<String, String> getNames(List<String> services) {
    List<String> names = []
    for (int i = 0; i < services.size(); i++) {
        names += sanitizeObjectName("${env.BUILD_TAG}-${services[i]}")
    }
    return names
}

List<String> env(List<String> services, List<String> names) {
    List<String> out = []
    if (services.contains('mongodb')) {
        out.add("MONGODB_HOST=${names[services.indexOf('mongodb')]}")
    }
    return out
}

String getDeploymentConfigYaml(String service, String name) {
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

String getServiceYaml(String service, String name) {
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
