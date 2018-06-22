def call(version, projects=["RHMAP", "FeedHenry", "AeroGear", "RAINCATCH", "AGDIGGER", "AGPUSH"]) {
    sh """jira-miner populate "project in (${projects.join(',')}) AND fixVersion in (${version}) and (status in ('Dev Complete', 'Ready for QA', Resolved, Done, Verified) or (status = Closed and resolution = Done)) and type in ('Feature Request', Bug, Improvement, Documentation,Enhancement,Clarification,'New Feature')"
    """
}
