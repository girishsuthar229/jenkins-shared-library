def call() {
    if (isUnix()) {
        sh 'npm ci || npm install'
        sh 'npx playwright install --with-deps'
    } else {
        bat 'npm ci || npm install'
        bat 'npx playwright install'
    }
}