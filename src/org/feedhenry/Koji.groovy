package org.feedhenry

import com.cloudbees.groovy.cps.NonCPS

@Grab('org.codehaus.groovy:groovy-xmlrpc:0.8')
import groovy.net.xmlrpc.XMLRPCServerProxy

class Koji implements Serializable {

    public static final int BUILD_INPROGRESS  = 0
    public static final int BUILD_COMPLETE    = 1
    public static final int BUILD_DELETED     = 2
    public static final int BUILD_FAILED      = 3
    public static final int BUILD_CANCELED    = 4

    final String url

    Koji(String url) {
        this.url = url
    }

    @NonCPS
    Map<String, String> getBuild(String nvr) {
        final XMLRPCServerProxy koji = new XMLRPCServerProxy(url, true)

        // If the build doesn't exist, a String is returned, rather than a Map.
        // This just translates that to empty Map rather than empty String
        def ret = koji.getBuild(nvr)
        return ret == '' ? [:] : ret
    }

    @NonCPS
    Map<String, String> awaitBuild(String nvr, Integer maxSleepSeconds = 14400) {
        Map<String, String> build = getBuild(nvr)
        Integer sleepSeconds = 1

        while (build?.state == BUILD_INPROGRESS &&
               sleepSeconds < maxSleepSeconds + 1) {
            Thread.sleep(sleepSeconds)
            sleepSeconds = sleepSeconds * 2

            build = getBuild(nvr)
        }

        return build ?: [:]
    }

    @NonCPS
    List<Map<String, String>> listBuilds(Integer packageID) {
        final XMLRPCServerProxy koji = new XMLRPCServerProxy(url, true)
        return koji.listBuilds(packageID)
    }

    @NonCPS
    List<Map<String, String>> listArchives(int buildId) {
        final XMLRPCServerProxy koji = new XMLRPCServerProxy(url, true)
        return koji.listArchives(buildId)
    }
}
