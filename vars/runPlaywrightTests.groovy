def call() {
    try {
        echo "🧪 Running Playwright tests..."

        if (isUnix()) {
            sh 'npx playwright test'
        } else {
            bat 'npx playwright test'
        }

        echo "Checking Allure results..."

        if (isUnix()) {
            sh 'ls -R allure-results || true'
        } else {
            bat 'dir allure-results'
        }

    } catch (err) {
        echo "⚠️ Playwright tests failed but continuing pipeline..."
        currentBuild.result = 'UNSTABLE'

    } finally {
        echo "📄 Test execution completed."
    }
}