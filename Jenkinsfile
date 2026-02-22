tools {
    maven 'Maven'       ← Jenkins finds Maven at /opt/homebrew/opt/maven/libexec
    jdk 'JDK21'         ← Jenkins switches to JDK 21
}pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    environment {
        LT_USERNAME     = credentials('lt-username')
        LT_ACCESS_KEY   = credentials('lt-access-key')
    }

    parameters {
        choice(name: 'TEST_SUITE', choices: ['Smoke', 'Regression', 'All', 'HyperExecute'], description: 'Select Test Suite to Run')
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Select Browser')
        choice(name: 'ENVIRONMENT', choices: ['QA', 'Staging', 'Production'], description: 'Select Environment')
    }

    stages {

        stage('📥 Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME ?: 'Devlop'}"
            }
        }

        stage('🔨 Build') {
            steps {
                sh 'mvn clean compile test-compile -q'
            }
        }

        stage('🔥 Smoke Tests') {
            when {
                expression { params.TEST_SUITE == 'Smoke' || params.TEST_SUITE == 'All' }
            }
            steps {
                sh """
                    mvn test \
                        -Dcucumber.filter.tags="@Smoke" \
                        -Dbrowser=${params.BROWSER} \
                        -Dexecution.mode=local
                """
            }
            post {
                always {
                    publishHTML(target: [
                        reportDir: 'src/test/resources/reports',
                        reportFiles: 'extent.html',
                        reportName: 'Smoke - Extent Report',
                        keepAll: true
                    ])
                }
            }
        }

        stage('🧪 Regression Tests') {
            when {
                expression { params.TEST_SUITE == 'Regression' || params.TEST_SUITE == 'All' }
            }
            steps {
                sh """
                    mvn test \
                        -Dcucumber.filter.tags="@Regression" \
                        -Dbrowser=${params.BROWSER} \
                        -Dexecution.mode=local
                """
            }
            post {
                always {
                    publishHTML(target: [
                        reportDir: 'src/test/resources/reports',
                        reportFiles: 'extent.html',
                        reportName: 'Regression - Extent Report',
                        keepAll: true
                    ])
                    publishHTML(target: [
                        reportDir: 'src/test/resources/reports',
                        reportFiles: 'qmetry-summary.html',
                        reportName: 'QMetry Summary Report',
                        keepAll: true
                    ])
                }
            }
        }

        stage('🚀 HyperExecute Cloud Tests') {
            when {
                expression { params.TEST_SUITE == 'HyperExecute' }
            }
            steps {
                sh '''
                    curl -L -o hyperexecute https://downloads.lambdatest.com/hyperexecute/linux/hyperexecute
                    chmod +x hyperexecute
                    ./hyperexecute \
                        --user "${LT_USERNAME}" \
                        --key "${LT_ACCESS_KEY}" \
                        --config hyperexecute.yaml
                '''
            }
        }
    }

    post {
        always {
            echo '📊 Archiving test reports...'
            archiveArtifacts artifacts: 'src/test/resources/reports/**/*', allowEmptyArchive: true
            archiveArtifacts artifacts: 'target/qmetry-reports/**/*', allowEmptyArchive: true
        }
        success {
            echo '✅ Pipeline PASSED!'
        }
        failure {
            echo '❌ Pipeline FAILED!'
            // Uncomment below to send email on failure
            // mail to: 'sathish.oruganti45@gmail.com',
            //      subject: "❌ Jenkins Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //      body: "Check: ${env.BUILD_URL}"
        }
    }
}
