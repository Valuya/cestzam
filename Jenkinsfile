#!groovy

pipeline {
    agent any
    parameters {
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip tests')
        booleanParam(name: 'FORCE_DEPLOY', defaultValue: false,
                description: 'Force deploy on feature branches (packages published on rc, master branches)')
        string(name: 'ALT_DEPLOYMENT_REPOSITORY', defaultValue: '', description: 'Alternative deployment repo')
        string(name: 'DOCKER_REPO', defaultValue: 'eu.gcr.io/lifeislife-cloud', description: 'Alternative deployment repo')
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage('Build & publish') {
            steps {
                script {
                    env.MVN_ARGS = "-Pthorntail"
                    env.MVN_GOALS = "clean package"
                    env.DO_DEPLOY = false
                    if (params.ALT_DEPLOYMENT_REPOSITORY != '') {
                        env.MVN_ARGS = "${env.MVN_ARGS} -DaltDeploymentRepository=${params.ALT_DEPLOYMENT_REPOSITORY}"
                    }
                    if (params.SKIP_TESTS) {
                        env.MVN_ARGS = "${env.MVN_ARGS} -DskipTests=true"
                    }
                    if (env.BRANCH_NAME == "master" || env.BRANCH_NAME == "rc" || params.FORCE_DEPLOY == true) {
                        env.MVN_GOALS = "clean deploy"
                        env.MVN_ARGS = "${env.MVN_ARGS}"
                        env.DO_DEPLOY = true
                    }
                }
                withMaven(maven: 'maven', mavenSettingsConfig: 'nexus-mvn-settings') {
                    sh "mvn $MVN_ARGS $MVN_GOALS"
                    script {
                        VERSION = sh(script: 'JENKINS_MAVEN_AGENT_DISABLED=true mvn help:evaluate -Dexpression=project.version -q -DforceStdout | tail -n1', returnStdout: true).trim()
                    }
                    stash(name: 'cestzam-ws-archive', includes: 'cestzam-ws/target/cestzam-ws-thorntail.jar')
                }
            }
        }
        stage('Build & push docker image') {
            agent {
                label 'docker'
            }
            steps {
                container('docker') {
                    unstash(name: 'cestzam-ws-archive')
                    sh "echo ${VERSION}"
                    script {
                        def image = docker.build("${params.DOCKER_REPO}/cestzam-ws:${VERSION}")
                        image.push()
                        image.push("${BRANCH_NAME}-latest")
                    }
                }
            }
        }
    }
}
