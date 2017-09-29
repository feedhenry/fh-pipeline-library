#!/usr/bin/groovy

def call(String name) {
    def chefServerNameSplit = (name.split('-') as List<String>)
    chefServerNameSplit.push("chef")
    def String chefServerCredentialsId = chefServerNameSplit.unique().join('-')
    return chefServerCredentialsId
}
