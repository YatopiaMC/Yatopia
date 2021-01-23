pipeline {
    agent { label 'slave' }
    options { timestamps() }
    stages {
        stage('Cleanup') {
            steps {
                scmSkip(deleteBuild: true, skipPattern:'.*\\[CI-SKIP\\].*')
                sh 'rm -rf ./target'
                sh 'rm -rf ./Paper/Paper-API ./Paper/Paper-Server ./Paper/work/Spigot/Spigot-API ./Paper/work/Spigot/Spigot-Server'
                sh 'rm -rf ./Yatopia-API ./Yatopia-Server'
                sh 'chmod +x ./gradlew'
            }
        }
        stage('Init project & submodules') {
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
                        ./gradlew build
                        ./gradlew publish
                        '''
                    }
                }
            }
        }
        stage('Build Launcher') {
            tools {
                jdk "OpenJDK 8"
            }
            steps {
                withMaven(
                    maven: '3',
                    mavenLocalRepo: '.repository',
                    publisherStrategy: 'EXPLICIT'
                ) {
                    sh '''
                        mkdir -p "./target"
                        ./gradlew paperclip
                        cp "yatopia-$mcver-paperclip.jar" "./target/yatopia-$mcver-paperclip-b$BUILD_NUMBER.jar"
                        '''
                }
            }
            post {
                success {
                    archiveArtifacts "target/*.jar"
                }
                failure {
                    cleanWs()
                }
            }
        }
    }
}
