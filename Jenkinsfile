pipeline {
    agent { label 'slave' }
    options { timestamps() }
    stages {
        stage('Cleanup') {
            steps {
                scmSkip(deleteBuild: true, skipPattern:'.*\\[CI-SKIP\\].*')
                sh 'git config --global gc.auto 0'
                sh 'rm -rf ./target'
                sh 'rm -rf ./Paper/Paper-API ./Paper/Paper-Server ./Paper/work'
                sh 'rm -rf ./Yatopia-API ./Yatopia-Server'
                sh 'chmod +x ./gradlew'
                sh './gradlew clean'
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
                        ./gradlew build publish
                        mkdir -p "./target"
                        basedir=$(pwd)
                        paperworkdir="$basedir/Paper/work"
                        mcver=$(cat "$paperworkdir/BuildData/info.json" | grep minecraftVersion | cut -d '"' -f 4)
                        
                        patchedJarPath="$basedir/Yatopia-Server/build/libs/yatopia-server-$mcver-R0.1-SNAPSHOT.jar"
                        vanillaJarPath="$paperworkdir/Minecraft/$mcver/$mcver.jar"

                        cd "$paperworkdir/Paperclip"
                        mvn -T 2C clean package -Dmcver="$mcver" -Dpaperjar="$patchedJarPath" -Dvanillajar="$vanillaJarPath" -Dstyle.color=never
                        cd "$basedir"

                        cp -v "$paperworkdir/Paperclip/assembly/target/paperclip-$mcver.jar" "./target/yatopia-$mcver-paperclip-b$BUILD_NUMBER.jar"
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
    }
}
