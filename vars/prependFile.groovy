def call(text, file) {
    writeFile file: "${file}_new", text: text, encoding: 'UTF-8'
    sh "cat ${file} >> ${file}_new"
    sh "mv ${file}_new ${file}"            
}