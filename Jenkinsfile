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

    }
}
