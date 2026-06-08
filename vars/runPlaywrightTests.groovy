def call() {
    if (isUnix()) {
        sh 'npx playwright test'
    } else {
        bat 'npx playwright test'
    }
}