package tests.library

import test.support.PipelineSpockTestBase

/**
 * How to unit test some vars DSL like shared code
 */
class eachComponentTestSpec extends PipelineSpockTestBase {

    def "test shared library code"() {
        def result = []

        given:
        def components = ["test_component"]
        def configs = ["test_component":[
          "gitHubOrg" : "test_org",
          "gitUrl" : "git@github:test/component",
          "gitHubUrl" : "github.com/org/component"
        ]]
        def body = {name, org, url, hubUrl, cfg -> result.add(name)}

        when:
        def script = loadScript('vars/eachComponent.groovy')
        script.call(components,configs,body)

        then:
        result == ["test_component"]
        printCallStack()
        assertJobStatusSuccess()
    }
}
