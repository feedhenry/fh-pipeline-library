#!/usr/bin/groovy

def call(Map params) {

    final String bucket = params.bucket
    final String filePattern = params.filePattern ?: '*.*'
    final String directory = params.directory ?: '.'

    dir(directory) {
        def files = findFiles(glob: filePattern)
        for (i = 0; i < files.size(); i++) {
            def f = files[i]
            def sourceFile = f.name
            def md5 = sh(
                    script: "md5sum ${f.path} | awk '{print \$1}'",
                    returnStdout: true
            ).trim()
            retry(4) {
                step([$class: 'S3BucketPublisher',
                        dontWaitForConcurrentBuildCompletion: false,
                        entries: [
                                [
                                        bucket: bucket,
                                        excludedFile: '',
                                        flatten: true,
                                        gzipFiles: false,
                                        keepForever: false,
                                        managedArtifacts: false,
                                        noUploadOnFailure: true,
                                        selectedRegion: 'eu-west-1',
                                        showDirectlyInBrowser: false,
                                        sourceFile: sourceFile,
                                        storageClass: 'STANDARD',
                                        uploadFromSlave: false,
                                        useServerSideEncryption: false
                                ]
                        ],
                        profileName: 's3',
                        userMetadata: [[key: 'md5', value: md5]]
                ])
            }
        }
    }

}
