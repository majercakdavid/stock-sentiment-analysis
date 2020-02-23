pipeline {
    agent any

    stages {
        stage('Compile') {
            steps {
                echo "Copying config file"

                configFileProvider(
                    [configFile(fileId: '2691b741-e831-45c0-9456-0ffa7f0e3bfd', targetLocation: 'twitter-kafka-scala/twitter-kafka-scala/config/application.conf'),
                    configFile(fileId: '19a58af0-4154-4406-be3c-efcdb60537b5', targetLocation: 'flink-processing/flink-processing/config/application.conf')]
                ) {
                    echo "Files copied"
                    echo "Compiling..."
                    dir("twitter-kafka-scala/twitter-kafka-scala") {
                        sh "/usr/bin/sbt compile"
                    }
                    dir("flink-processing/flink-processing") {
                        sh "/usr/bin/sbt compile"
                    }
                }
            }
        }

        stage('Assembly') {
            steps {
                echo "Packaging..."
                dir("twitter-kafka-scala/twitter-kafka-scala") {
                    sh "/usr/bin/sbt assembly"
                }
                dir("flink-processing/flink-processing") {
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

        stage('Deploy and run apps in docker') {
          steps {
            echo "Building twitter-kafka-producer docker image"
            sh "docker build -t twitter-kafka-producer -f TwitterKafka.Dockerfile ."

            echo "Building sentiment-analyser docker image"
            sh "docker build -t sentiment-analyser -f SentimentAnalyser.Dockerfile ."

            echo "Building flink-processing docker image"
            sh "docker build -t flink-processing -f FlinkProcessing.Dockerfile ."

            echo "Deploying apps docker images"
            sh "docker-compose -f docker-compose-apps.yml up -d --build"
          }
        }

    }
}
