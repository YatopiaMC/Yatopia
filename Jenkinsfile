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
                        ./gradlew clean build yatoclip publish
                        mkdir -p "./target"
                        basedir=$(pwd)
                        paperworkdir="$basedir/Paper/work"
                        mcver=$(cat "$paperworkdir/BuildData/info.json" | grep minecraftVersion | cut -d '"' -f 4)
                        cp "yatopia-$mcver-yatoclip.jar" "./target/yatopia-$mcver-yatoclip-b$BUILD_NUMBER.jar"
                        '''
                    }
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
