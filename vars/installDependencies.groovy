def call() {
    if (isUnix()) {
        sh 'npm install'
        sh 'npx playwright install --with-deps'
    } else {
        bat 'npm install'
        bat 'npx playwright install'
    }
}