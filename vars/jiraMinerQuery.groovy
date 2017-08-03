import org.feedhenry.Utils

def call(query, args) {
    def props = ""

    def utils = new Utils()

    for (def arg in utils.mapToList(args)) {
      props += " --${arg[0]} ${arg[1]}"
    }

    return sh (returnStdout:true, script: "jira-miner query ${query} ${props}")
}