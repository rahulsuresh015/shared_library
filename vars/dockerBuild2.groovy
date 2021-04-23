def call(String registryCred = 'a', String registryin = 'a', String docTag = 'a', String contName = 'a', String grepo = 'a', String gbranch = 'a', String gitcred = 'a', String sshcred = 'a', String sship = 'a') {

pipeline {
		environment { 
		  dockerhubCredID = "${registryCred}"
		  dockerhubRegistry = "$registryin" 	
		  dockerTag = "${docTag}_$BUILD_NUMBER"
		  containerName = "${contName}"
		  gitRepo = "${grepo}"
		  gitBranch = "${gbranch}"
		  gitCredID = "${gitcred}"
      sshCredID = "${sshcred}"
      sshHost = "${sship}"
		}
    agent any

    stages {
        stage('Stage1:GIT') {
            steps {
                git branch: '$gitBranch', credentialsId: ['$gitCredID'], url: '$gitRepo'
                
            }
        } 
        stage('Stage2:BUILD') {
            steps {
                sh 'docker build -t $dockerhubRegistry:$dockerTag_$BUILD_NUMBER .'
                
            }
        }
        stage('Stage3:PUSH HUB') {
            steps {
                script {
                    docker.withRegistry( '', '$dockerhubCredID' ) {
                        sh 'docker tag $dockerhubRegistry:$dockerTag_$BUILD_NUMBER $dockerhubRegistry:$dockerTag_$BUILD_NUMBER'
                        sh 'docker push $dockerhubRegistry:$dockerTag_$BUILD_NUMBER'
                    }
                }    
            }
        }
        stage('Stage4:DEOPLY') {
            steps {
                sshagent (credentials: ['$sshCredID']) {
		              sh "ssh -o StrictHostKeyChecking=no -l ubuntu $sshHost ls"           
                }
            }
        }  
    }
}  
}
