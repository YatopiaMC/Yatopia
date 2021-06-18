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
                sh 'rm -rf .gradle'
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
                        sh'''
                        ./gradlew build publish
                        mkdir -p "./target"
                        paperworkdir=".gradle/caches/paperweight/upstreams/paper/work"
                        mcver=$(cat "$paperworkdir/BuildData/info.json" | grep minecraftVersion | cut -d '"' -f 4)
                        cp -v "build/libs/Yatopia-$mcver-R0.1-SNAPSHOT.jar" "./target/yatopia-$mcver-paperclip-b$BUILD_NUMBER.jar"
                        '''
                    }
                }
            }
        }

        stage('Archive Jars') {
            steps {
                archiveArtifacts(artifacts: 'target/*.jar', fingerprint: true)
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
