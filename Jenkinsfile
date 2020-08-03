pipeline {
    agent any
    tools {
        jdk 'jdk8'
        gradle 'default'
    }
    options {
        buildDiscarder(logRotator(artifactNumToKeepStr: '5'))
    }
    stages {
        stage('Build') {
            steps {
                sh 'gradle clean build'
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
    }
}