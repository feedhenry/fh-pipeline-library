#!/usr/bin/groovy

def call(body) {
    def taskFile = 'tasks/configs.js'

    if (!fileExists(taskFile)) {
        writeFile file: taskFile, text: '''\'use strict\';

module.exports = function(grunt) {

  grunt.config.merge({
    configs: {
      files: ["global.json", \'global-forms.json\']
    }
  });

  var templateHelper = require(\'./lib/templateHelper\').init(grunt);
  var _ = require(\'underscore\');
  var url = require(\'url\');

  function updateTemplateAppConfig(filepath, templateAppConfigs) {
    var globalJson = grunt.file.readJSON(filepath);

    templateHelper.walkObjects(globalJson.show, function (obj) {
      if (_.has(obj, \'githubUrl\')) {
        var templateAppConfig = {};
        var parsedUrl = url.parse(obj.githubUrl, true, true);
        var tarry = parsedUrl.path.replace(/.git$/, \'\').replace(/^\\//, \'\').split(\'/\');
        templateAppConfig.githubUrl = obj.githubUrl;
        templateAppConfig.baseBranch = obj.repoBranch;
        templateAppConfig.org = tarry[0];
        templateAppConfig.name = tarry[1];
        templateAppConfig.jira = "";
        templateAppConfig.type = "template-apps";
        templateAppConfig.labels = {};
        templateAppConfig.labels.rhmap3 = false;
        templateAppConfig.labels.rhmap4 = false;
        templateAppConfigs.push(templateAppConfig);
      }
    });
  }

  grunt.registerMultiTask(\'configs\', \'Generate template app config file\', function () {
    var fileName = grunt.option(\'file-name\') || \'COMPONENTS.json\';
    var templateAppConfigs = [];

    this.filesSrc.forEach(function (filepath) {
      updateTemplateAppConfig(filepath, templateAppConfigs);
    });

    var components = {};
    components.components = templateAppConfigs;
    grunt.file.write(fileName, JSON.stringify(components, null, \'  \') + \'\\n\');
  });

};
'''
    }
}