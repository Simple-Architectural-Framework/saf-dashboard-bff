pipeline {
    agent any
    stages {
        stage('Build & Package') {
            steps {
                sh './gradlew bootJar'
            }
        }
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t saf-dashboard-bff:latest .'
            }
        }
        stage('Load into Minikube') {
            steps {
                sh 'minikube image load saf-dashboard-bff:latest'
            }
        }
    }
}