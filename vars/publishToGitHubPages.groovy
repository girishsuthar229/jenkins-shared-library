def call(Map config = [:]) {

    def repoUrl = config.repoUrl ?: error("repoUrl is required")
    def branch  = config.branch ?: 'gh-pages'
    def reportDir = config.reportDir ?: 'allure-report'
    def gitUser = config.gitUser ?: 'Jenkins CI'
    def gitEmail = config.gitEmail ?: 'jenkins@example.com'
    def credentialsId = config.credentialsId ?: error("credentialsId is required")
    def publicUrl = config.publicUrl ?: ''
    def emailTo = config.emailTo ?: ''  // ⚡ Make sure you define this

    echo "Publishing report to GitHub Pages..."

    withCredentials([string(credentialsId: credentialsId, variable: 'GH_TOKEN')]) {

        if (isUnix()) {
            sh """
                git config --global user.email "${gitEmail}"
                git config --global user.name "${gitUser}"

                rm -rf gh-pages
                git clone --branch ${branch} https://${GH_TOKEN}@${repoUrl.replace('https://','')} gh-pages

                rm -rf gh-pages/*
                cp -r ${reportDir}/* gh-pages/

                cd gh-pages
                git add .

                git diff --cached --quiet || git commit -m "Update Allure report - build ${env.BUILD_NUMBER}"

                git push origin ${branch}
            """
        } else {
            bat """
                git config --global user.email "${gitEmail}"
                git config --global user.name "${gitUser}"

                rmdir /s /q gh-pages
                git clone --branch ${branch} https://${GH_TOKEN}@${repoUrl.replace('https://','')} gh-pages

                rmdir /s /q gh-pages
                xcopy ${reportDir}\\* gh-pages /E /I /Y

                cd gh-pages
                git add .

                git diff --cached --quiet || git commit -m "Update Allure report - build ${env.BUILD_NUMBER}"

                git push origin ${branch}
            """
        }
    }

    if (publicUrl) {
        echo "🌐 Public Report URL: ${publicUrl}"
    }

    // ✅ Send email if emailTo is defined
    if (emailTo?.trim()) {
        emailext(
            subject: "Playwright Test Results - Build #${env.BUILD_NUMBER}",
            body: """
                <h3>Test Execution Completed</h3>
                <p><b>Build:</b> ${env.BUILD_NUMBER}</p>
                <p><b>Build URL:</b> ${env.BUILD_URL}</p>
                ${publicUrl ? "<p><b>GitHub Pages Report:</b> <a href='${publicUrl}'>${publicUrl}</a></p>" : ""}
            """,
            mimeType: 'text/html',
            to: emailTo
        )
    } else {
        echo "No email recipient provided, skipping email step."
    }
}