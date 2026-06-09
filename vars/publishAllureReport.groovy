def call() {
    echo "Checking Allure results..."

    sh 'ls -R allure-results || true'

    allure([
        includeProperties: false,
        jdk: '',
        results: [[path: 'allure-results']]
    ])
}