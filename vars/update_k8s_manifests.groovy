#!/usr/bin/env groovy

/**
 * Update Kubernetes manifests with new image tags
 */
def call(Map config = [:]) {
    def imageTag = config.imageTag ?: error("Image tag is required")
    def manifestsPath = config.manifestsPath ?: 'kubernetes'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'
    def gitUserName = config.gitUserName ?: 'Suchit CI'
    def gitUserEmail = config.gitUserEmail ?: 'suchit@example.com'
    
    echo "Updating Kubernetes manifests with image tag: ${imageTag}"
    
    withCredentials([usernamePassword(
        credentialsId: gitCredentials,
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {
        // Configure Git
        sh """
            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"
        """
        
        // Update deployment manifests with new image tags
        sh """
            # Update backend deployment
            sed -i "s|image: suchitdeshmukh/wanderlust-backend-beta:.*|image: suchitdeshmukh/wanderlust-backend-beta:${imageTag}|g" ${manifestsPath}/backend.yaml
            
            # Update frontend deployment
            sed -i "s|image: suchitdeshmukh/wanderlust-frontend-beta:.*|image: suchitdeshmukh/wanderlust-frontend-beta:${imageTag}|g" ${manifestsPath}/frontend.yaml
            
            # Check for changes
            if git diff --quiet; then
                echo "No changes to commit"
            else
                # Commit and push changes
                git add ${manifestsPath}/*.yaml
                git commit -m "Update image tags to ${imageTag} [ci skip]"
                
                # Push changes to your GitHub repo
                git remote set-url origin https://\${GIT_USERNAME}:\${GIT_PASSWORD}@github.com/YOUR_GITHUB_USERNAME/Wanderlust-Mega-Project.git
                git push origin HEAD:\${GIT_BRANCH}
            fi
        """
    }
}
