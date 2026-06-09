def call(Map config = [:]) {
    def repoUrl = config.repoUrl ?: error("repoUrl is required")
    def branch = config.branch ?: 'gh-pages'
    def reportDir = config.reportDir ?: 'allure-report'
    def gitUser = config.gitUser ?: 'Jenkins CI'
    def gitEmail = config.gitEmail ?: 'jenkins@example.com'
    def credentialsId = config.credentialsId ?: error("credentialsId is required")
    def publicUrl = config.publicUrl ?: ''
    def emailTo = config.emailTo ?: ''

    withCredentials([string(credentialsId: credentialsId, variable: 'GH_TOKEN')]) {

        if (isUnix()) {
            sh """
                git config --global user.email "${gitEmail}"
                git config --global user.name "${gitUser}"

                rm -rf gh-pages

                # Try to clone branch, if it fails, init a new repo
                if ! git clone --branch ${branch} https://${GH_TOKEN}@${repoUrl.replace('https://','')} gh-pages; then
                    mkdir gh-pages
                    cd gh-pages
                    git init
                    git checkout -b ${branch}
                else
                    cd gh-pages
                fi

                # Clean old report and copy new report
                rm -rf *
                cp -r ../${reportDir}/* .

                git add .
                # Only commit if there are changes
                git diff --cached --quiet || git commit -m "Update Allure report - build ${env.BUILD_NUMBER}"

                # Push branch (create it on remote if missing)
                git push origin ${branch} --set-upstream
            """
        } else {
            bat """
                git config --global user.email "${gitEmail}"
                git config --global user.name "${gitUser}"

                rmdir /s /q gh-pages
                git clone --branch ${branch} https://${GH_TOKEN}@${repoUrl.replace('https://','')} gh-pages || (
                    mkdir gh-pages
                    cd gh-pages
                    git init
                    git checkout -b ${branch}
                )
                cd gh-pages

                rmdir /s /q *
                xcopy ..\\${reportDir}\\* . /E /I /Y

                git add .
                git diff --cached --quiet || git commit -m "Update Allure report - build ${env.BUILD_NUMBER}"

                git push origin ${branch} --set-upstream
            """
        }
    }

    if (publicUrl) {
        echo "🌐 Public Report URL: ${publicUrl}"
    }

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