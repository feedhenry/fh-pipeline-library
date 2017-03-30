#!/usr/bin/groovy

def call(components, componentsConfig, body) {
    for (i = 0; i < components.size(); i++) {
        def index = i
        def componentName = components[index]
        if (componentsConfig[componentName]) {
            def componentConfig = componentsConfig[componentName]
            def componentGitHubOrg = componentConfig['gitHubOrg']
            def componentGitUrl = componentConfig['gitUrl']
            def componentGitHubUrl = componentConfig['gitHubUrl']
            body(componentName, componentGitHubOrg, componentGitUrl, componentGitHubUrl, componentConfig)
        } else {
            print "No component config found for '${componentName}'!"
        }
    }
}
