pipeline {
    agent { label 'slave' }
    options { timestamps() }

    environment {
        discord_webhook1 = credentials('yatopia_discord_webhook')
    }

    stages {
        stage('Cleanup') {
            tools {
                jdk "OpenJDK 8"
            }
            steps {
                scmSkip(deleteBuild: true, skipPattern:'.*\\[CI-SKIP\\].*')
                sh 'git config --global gc.auto 0'
                sh 'rm -rf ./target'
                sh 'rm -rf ./Paper/Paper-API ./Paper/Paper-Server'

                // sh 'mv ./Paper/work/Minecraft ./ || true' 
                // sh 'rm -fr ./Paper/work/*'
                // sh 'mv ./Minecraft ./Paper/work/ || true'


                sh 'rm -rf ./Yatopia-API ./Yatopia-Server'
                sh 'chmod +x ./gradlew'
                sh './gradlew clean'
            }
        }
        stage('Init project & submodules') {
            tools {
                jdk "OpenJDK 8"
            }
            steps {
                withMaven(
                    maven: '3',
                    mavenLocalRepo: '.repository',
                    publisherStrategy: 'EXPLICIT',
                ) {
                    sh './gradlew initGitSubmodules'
                }
            }
        }
        stage('Decompile & apply patches') {
            tools {
                jdk "OpenJDK 8"
            }
            steps {
                withMaven(
                    maven: '3',
                    mavenLocalRepo: '.repository',
                    publisherStrategy: 'EXPLICIT',
                ) {
                    sh '''
                    ./gradlew setupUpstream
                    ./gradlew applyPatches
                    '''
                }
            }
        }
        stage('Build') {
            tools {
                jdk "OpenJDK 8"
            }
            steps {
                withMaven(
                    maven: '3',
                    mavenLocalRepo: '.repository',
                    publisherStrategy: 'EXPLICIT'
                ) {
                    withCredentials([usernamePassword(credentialsId: 'jenkins-deploy', usernameVariable: 'ORG_GRADLE_PROJECT_mavenUsername', passwordVariable: 'ORG_GRADLE_PROJECT_mavenPassword')]) {
                        sh '''
                        ./gradlew build publish yatoclip
                        mkdir -p "./target"
                        basedir=$(pwd)
                        paperworkdir="$basedir/Paper/work"
                        mcver=$(cat "$paperworkdir/BuildData/info.json" | grep minecraftVersion | cut -d '"' -f 4)

                        cp -v "./yatopia-$mcver-yatoclip.jar" "./target/yatopia-$mcver-yatoclip-b$BUILD_NUMBER.jar"
                        '''
                    }
                }
            }
        }
            
        stage('Archive Jars') {
            steps {
                archiveArtifacts(artifacts: 'target/*.jar', fingerprint: true)
            }
            post {
                always {
                    cleanWs()
                }
            }
        }

        stage('Discord Webhook') {
            steps {
                script {
                    discordSend description: "Yatopia Jenkins Build", footer: "Yatopia", link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: discord_webhook1
                }
            }
        }
    }
}
