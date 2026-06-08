def call(String repoUrl) {
    git url: repoUrl, branch: 'main'
}