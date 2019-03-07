pipeline {
    agent any
    tools {
        maven 'Maven 3'
        jdk 'Java 8'
    }
    options {
        buildDiscarder(logRotator(artifactNumToKeepStr: '5'))
    }
    stages {
        stage('Build') {
            steps {
                sh 'gradlew build'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'out/Matrix-*.jar', fingerprint: true
                }
                discordSend description: 'Matrix Pipeline Build', link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: 'https://discordapp.com/api/webhooks/459174129162125312/Tb7yNBLwcJwFLz0hQGpDvKcwl697cHc9-JxekhQGGVpE3TQEKEo0VVXpl37v0ndUeQmv'
            }
        }
    }
}