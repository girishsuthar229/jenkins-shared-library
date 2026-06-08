def call() {
    try {
        echo "🧪 Running Playwright tests..."

        if (isUnix()) {
            sh 'npx playwright test --reporter=html'
        } else {
            bat 'npx playwright test --reporter=html'
        }

    } catch (err) {
        echo "⚠️ Playwright tests failed but continuing pipeline..."
        currentBuild.result = 'UNSTABLE'

    } finally {
        echo "📄 Test execution completed. Reports should be generated (playwright-report/)"
    }
}