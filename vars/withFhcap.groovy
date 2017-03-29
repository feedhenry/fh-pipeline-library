#!/usr/bin/groovy

def call(body) {

    node('ruby-fhcap') {

        step([$class: 'WsCleanup'])

        //Install and setup fhcap-cli
        sh "gem install fhcap-cli --no-ri --no-rdoc"
        sh "fhcap --version"
        sh "echo '{\n" +
                "   \"repos_dir\": \"'\$WORKSPACE'\",\n" +
                "   \"fh_src_dir\": \"'\$WORKSPACE'\",\n" +
                "   \"repos\": {\n" +
                "       \"fhcap\": {\n" +
                "           \"url\": \"git@github.com:fheng/fhcap.git\",\n" +
                "           \"archives\": {\n" +
                "               \"cookbooks\": \"https://s3-eu-west-1.amazonaws.com/fhcap/fhcap-cookbooks-0fa68747b476f7ee555a94483877969a30133e76.tgz\"\n" +
                "           }\n" +
                "       }\n" +
                "   },\n" +
                "   \"knife_dir\": \"'\$WORKSPACE'/fhcap/.chef\",\n" +
                "   \"knife\": {},\n" +
                "   \"providers\": {}\n" +
                "}' > \"fhcap.json\""

        env.PATH = "${PATH}:/home/jenkins/bin"
        env.FHCAP_CFG_FILE = "${WORKSPACE}/fhcap.json"

        body()
    }
}
