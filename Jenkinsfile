/* uses sbt, which i installed with homebrew. */
/* this works without requiring the 'sbt plugin'. */

pipeline {
    agent any

    stages {

        stage('Compile') {
            steps {
                echo "Compiling..."
                dir("twitter-kafka-scala/twitter-kafka-scala") {
                    sh "/usr/bin/sbt compile"
                }
            }
        }

        // stage('Test') {
        //     steps {
        //         echo "Testing..."
        //         sh "/usr/local/bin/sbt test"
        //     }
        // }

        // stage('Package') {
        //     steps {
        //         echo "Packaging..."
        //         sh "/usr/local/bin/sbt package"
        //     }
        // }

        stage('Assembly') {
            steps {
                echo "Packaging..."
                dir("twitter-kafka-scala/twitter-kafka-scala") {
                    sh "/usr/bin/sbt assembly"
                }
            }
        }

        stage('Deploy and run dependencies') {
          steps {
            echo "Deploying docker images for dependencies"
            sh "docker-compose -f docker-compose-elk.yml up -d --build"
            sh "docker-compose -f docker-compose-kafka.yml up -d --build"
          }
        }

        stage('Deploy and run app in docker') {
          steps {
            echo "Building app docker images"
            sh "docker build -t twitter-kafka-producer ."
            echo "Deploying app docker images"
            sh "docker-compose -f docker-compose-apps.yml up -d --build"
          }
        }

    }
}
