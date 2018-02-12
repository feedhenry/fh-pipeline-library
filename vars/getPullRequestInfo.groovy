import java.util.regex.Pattern

@NonCPS
def getPRInfo() {
    try {
        def r = Pattern.compile(".*//.*/(?<org>.*)/(?<repo>.*)/pull/(?<id>.*)")
        def m = r.matcher(env.CHANGE_URL)
        m.matches()
        return ["org" : m.group("org"), "repo": m.group("repo"), "id": m.group("id")]
    } catch(e) {
        print e
        return []
    }
}

def call() {
  return getPRInfo()
}