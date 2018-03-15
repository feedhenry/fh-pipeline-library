#!/usr/bin/groovy

/**
 *
 * Returns configuration for the given provider and template.
 *
 * The template name is used to search for appropriate config in the supplied providers object.
 * e.g. A template with the name "ocp-multi-infra-platform" will check for config in the order
 * ocp-multi-infra-platform -> ocp-multi-infra -> ocp-multi -> ocp -> default
 *
 * @param providerId the provider id to retrieve config for.
 * @param providers the providers config object containing all provider configuration.
 * @param template the template to retrieve config for.
 * @return a config object for the given provider and template.
 */
def call(String providerId, Map providers, String template) {
    def providerCfg = null
    String delimiter = '-'
    List<String> tmplParts = template.split(delimiter) as List<String>
    while (tmplParts.size() > 0) {
        String tmplStr = tmplParts.join(delimiter)
        providerCfg = providers[providerId].providerConfig[tmplStr]
        if (providerCfg) {
            break
        }
        tmplParts.pop()
    }
    (providers[providerId].providerConfig['default'] ?: [:]) + (providerCfg ?: [:])
}
