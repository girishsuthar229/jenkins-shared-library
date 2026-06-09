def call() {
    echo "Checking Allure results..."

    if (isUnix()) {
        sh 'ls -R allure-results || true'
    } else {
        bat 'dir allure-results'
    }

    allure([
        includeProperties: false,
        jdk: '',
        results: [[path: 'allure-results']]
    ])
}