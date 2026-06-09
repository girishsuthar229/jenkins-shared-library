def call(String repoUrl) {
    checkout([
        $class: 'GitSCM',
        branches: [[name: '*/main']],
        userRemoteConfigs: [[url: repoUrl]]
    ])
}