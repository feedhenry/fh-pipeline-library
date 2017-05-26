#!/usr/bin/groovy

def call(settings) {
   def defaul_settings = [
         $class: 'CoberturaPublisher',
         autoUpdateHealth: false, 
         autoUpdateStability: false, 
         coberturaReportFile: '**/coverage/cobertura-coverage.xml', 
         failNoReports: true, 
         failUnhealthy: false, 
         failUnstable: false, 
         maxNumberOfBuilds: 0, 
         onlyStable: false, 
         sourceEncoding: 'UTF_8', 
         zoomCoverageChart: true]
   def allsettings = defaul_settings + settings
   step(allsettings)
}
