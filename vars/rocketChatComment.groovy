#!groovy

import groovy.json.JsonOutput

def call(promptText, configId="rocket.json") {
      
  def config = getParsedJsonConfigFile(configId)
  httpRequest httpMode: 'POST',
    contentType: 'APPLICATION_JSON',
    requestBody: JsonOutput.toJson([
        text:"@${userThatTriggeredBuild()}, ${promptText}",
        attachments:[
          [title: "${env.JOB_BASE_NAME} build ${env.BUILD_ID as int}", title_link:"${env.BUILD_URL}/console"]
        ]
    ]),
    customHeaders: [],
    url: config.url,
    validResponseCodes: '100:399' 
   
}
