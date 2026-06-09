def call() {
    echo "Publishing Allure Report..."

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