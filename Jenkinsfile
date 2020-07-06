pipeline {
    agent any
    tools {
        jdk 'jdk8'
    }
    options {
        buildDiscarder(logRotator(artifactNumToKeepStr: '5'))
    }
    stages {
        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'out/matrix-*.jar', fingerprint: true
                }
                always {
                    discordSend description: 'Matrix Pipeline Build', link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: 'https://discordapp.com/api/webhooks/720836731393540109/mx1TEQu0KhOTeS8HA8z_3ezR6D7gbdHejnGvtS1BWMUVjwBELCHQNDNt0ODWARm0rDDv'
                }
            }
        }
        stage('Publish') {
            when {
                // Only say hello if a "greeting" is requested
                expression { env.JOB_NAME.endsWith("/master") }
            }
            steps {
                sh './gradlew publish'
            }
        }
    }
}