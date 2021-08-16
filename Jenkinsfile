podTemplate(
    cloud: 'openshift',
    containers: [
        containerTemplate(
            name: 'jnlp',
            image: 'docker-registry.default.svc:5000/ruby-cicd-uat/jenkins-agent-maven-35-rhel7'
        ),
        containerTemplate(
            name: 'maven',
            image: 'docker-registry.default.svc:5000/ruby-cicd-uat/maven:3.6.3-openjdk-11',
            envVars: [
                envVar(key: 'MAVEN_OPTS', value: '-Dmaven.repo.local=.m2/repository'),
                envVar(key: 'MAVEN_CLI_OPTS', value: '-s .m2/settings.xml --batch-mode')
            ],
            ttyEnabled: true,
            command: 'cat'
        )
    ]
){
    try {
        timeout(time: 20, unit: 'MINUTES') {
            node(POD_LABEL) {
                stage('Pre-checkout'){
                    container('jnlp') {
                        sh 'git config --global http.sslVerify false'
                    }
                }

                stage('Checkout'){
                    container('jnlp') {
                        checkout scm: [
                            $class: 'GitSCM',
                            branches: [[name: '$BRANCH_TO_BUILD']],
                            doGenerateSubmoduleConfigurations: false,
                            extensions: [[$class: 'LocalBranch', localBranch: '**']],
                            submoduleCfg: [],
                            userRemoteConfigs: [[
                                credentialsId: 'max-gitlab',
                                url: 'https://git.btu.pccw.com/ruby/common/common-library.git'
                            ]]
                        ]
                    }
                }

                stage('Build'){
                    container('maven') {
                        sh '''
                        mvn \$MAVEN_CLI_OPTS clean package -DskipTests
                        '''
                    }
                }

                stage('Package') {
                    container('maven') {
                        withCredentials([[
                            $class: 'UsernamePasswordMultiBinding',
                            credentialsId: 'nexus-jenkins-user-credentials',
                            usernameVariable: 'NEXUS_USERNAME',
                            passwordVariable: 'NEXUS_PASSWORD'
                        ]]) {
                            sh '''
                            mvn \$MAVEN_CLI_OPTS deploy:deploy-file \
                            -DgroupId=\$(mvn \$MAVEN_CLI_OPTS org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.groupId -q -DforceStdout) \
                            -DartifactId=\$(mvn \$MAVEN_CLI_OPTS org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.artifactId -q -DforceStdout) \
                            -Dversion=\$(mvn \$MAVEN_CLI_OPTS org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.version -q -DforceStdout) \
                            -Dpackaging=jar \
                            -DrepositoryId=nexus \
                            -Durl=http://nexus3.ruby-cicd-uat.svc.cluster.local:8081/repository/maven-snapshots \
                            -Dfile=\$(find target -maxdepth 1 -type f -name '*.jar')
                            '''
                        }
                    }
                }
            }
        }
    } catch (err) {
        echo 'in catch block'
        echo "Caught: $err"
        currentBuild.result = 'FAILURE'
        throw err
    }
}
