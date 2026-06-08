def call() {
    sh 'npm install'
    sh 'npx playwright install --with-deps'
}