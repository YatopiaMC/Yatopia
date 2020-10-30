pipeline {
    agent { label 'slave' }
    options { timestamps() }
    stages {
        stage('Cleanup') {
            steps {
                scmSkip(deleteBuild: true, skipPattern:'.*\\[CI-SKIP\\].*')
                sh 'rm -rf ./target'
                sh 'rm -rf ./Tuinity/Paper/Paper-API ./Tuinity/Paper/Paper-Server ./Tuinity/Paper/work/Spigot/Spigot-API ./Tuinity/Paper/work/Spigot/Spigot-Server'
                sh 'rm -rf ./Tuinity/Tuinity-API ./Tuinity/Tuinity-Server ./Tuinity/mc-dev'
                sh 'rm -rf ./Yatopia-API ./Yatopia-Server'
                sh 'chmod +x ./scripts/*.sh'
            }
        }
        stage('Init project & submodules') {
            steps {
                sh './yatopia init'
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
                        set -e
                        source "./scripts/functions.sh"
                        basedir
                        $scriptdir/updateUpstream.sh "$basedir" false true || exit 1
                        set -e
                        $scriptdir/applyPatches.sh "$basedir" || exit 1
                    '''
                }
            }
        }
        stage('Build API') {
            tools {
                jdk "OpenJDK 8"
            }
            steps {
                withMaven(
                    maven: '3',
                    mavenLocalRepo: '.repository',
                    publisherStrategy: 'EXPLICIT'
                ) {
                    sh 'mvn -N install org.apache.maven.plugins:maven-deploy-plugin:2.8.2:deploy'
                    sh 'cd Yatopia-API && mvn install org.apache.maven.plugins:maven-deploy-plugin:2.8.2:deploy'
                    sh 'cd ./Tuinity/Paper/Paper-MojangAPI && mvn install'
                }
            }
        }
        stage('Build Server') {
            tools {
                jdk "OpenJDK 8"
            }
            steps {
                withMaven(
                    maven: '3',
                    mavenLocalRepo: '.repository',
                    publisherStrategy: 'EXPLICIT'
                ) {
                    sh 'cd Yatopia-Server && mvn install org.apache.maven.plugins:maven-deploy-plugin:2.8.2:deploy -DaltDeploymentRepository=codemc-snapshots::default::https://repo.codemc.org/repository/nms-local/'
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
                        basedir=$(pwd)
                        paperworkdir="$basedir/Tuinity/Paper/work"
                        mcver=$(cat "$paperworkdir/BuildData/info.json" | grep minecraftVersion | cut -d '"' -f 4)
                        serverjar="$basedir/Yatopia-Server/target/yatopia-$mcver.jar"
                        vanillajar="$paperworkdir/Minecraft/$mcver/$mcver.jar"
                        (
                            cd "$paperworkdir/Paperclip"
                            mvn clean package "-Dmcver=$mcver" "-Dpaperjar=$serverjar" "-Dvanillajar=$vanillajar"
                        )
                        mkdir -p "./target"
                        cp "$paperworkdir/Paperclip/assembly/target/paperclip-$mcver.jar" "./target/yatopia-$mcver-paperclip-b$BUILD_NUMBER.jar"
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
