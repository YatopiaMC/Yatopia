pipeline {
    agent { label 'slave' }
    options { timestamps() }

    environment {
        discord_webhook1 = credentials('yatopia_discord_webhook')
    }

    stages {
        stage('Cleanup') {
            tools {
                jdk "OpenJDK 16"
            }
            steps {
                scmSkip(deleteBuild: true, skipPattern:'.*\\[CI-SKIP\\].*')
                sh 'git config --global gc.auto 0'
                sh 'rm -rf ./target'
                sh 'rm -rf ./Yatopia-API ./Yatopia-Server'
                sh 'chmod +x ./gradlew'
                sh './gradlew clean'
            }
        }
        stage('Decompile & apply patches') {
            tools {
                jdk "OpenJDK 16"
            }
            steps {
                withMaven(
                    maven: '3',
                    mavenLocalRepo: '.repository',
                    publisherStrategy: 'EXPLICIT',
                ) {
                    sh '''
                    ./gradlew applyPatches
                    '''
                }
            }
        }
        stage('Build') {
            tools {
                jdk "OpenJDK 16"
            }
            steps {
                withMaven(
                    maven: '3',
                    mavenLocalRepo: '.repository',
                    publisherStrategy: 'EXPLICIT'
                ) {
                    withCredentials([usernamePassword(credentialsId: 'jenkins-deploy', usernameVariable: 'ORG_GRADLE_PROJECT_mavenUsername', passwordVariable: 'ORG_GRADLE_PROJECT_mavenPassword')]) {
                        sh './gradlew generatePaperclipPatch publish' // when paper fixes paperclip for forks then use - ./gradlew paperclipJar publish  
                        // cp -v "$paperworkdir/Paperclip/assembly/target/paperclip-$mcver.jar" "./target/yatopia-$mcver-paperclip-b$BUILD_NUMBER.jar" - this code needs to be reworked
                    }
                }
            }
        }

        stage('Discord Webhook') {
            steps {
                script {
                    discordSend description: "Yatopia Jenkins Build", footer: "Yatopia", link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: discord_webhook1
                }
            }
           post {
                always {
                    cleanWs()
                }
            }
        }
    }
}
