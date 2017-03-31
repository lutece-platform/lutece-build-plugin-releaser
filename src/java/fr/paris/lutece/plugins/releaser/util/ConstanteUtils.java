package fr.paris.lutece.plugins.releaser.util;

public class ConstanteUtils
{
    public static final int CONSTANTE_ID_NULL = -1;
    public static final String CONSTANTE_SEPARATOR_SLASH = "/";
    public static final String CONSTANTE_TAGS = "tags";
    public static final String CONSTANTE_TRUNK = "trunk";
    public static final String CONSTANTE_EMPTY_STRING = "";
    public static final String CONSTANTE_MAX_RELEASE_CONTEXT_KEY = "max_release_context_key";
    public static final String CONSTANTE_RELEASE_CONTEXT_PREFIX = "release_context_";
    public static final String CONSTANTE_COMPONENT_PROJECT_PREFIX = "component_project_prefix_";
    
    public static final String ATTRIBUTE_RELEASER_USER="releaser_user";
    public static final String CONSTANTE_SUFFIX_GIT="scm:git";
    public static final String CONSTANTE_POM_XML = "pom.xml";
    
    public static final String BEAN_WORKFLOW_RELEASE_CONTEXT_SERVICE= "releaser.workflowReleaseContextService";
    public static final String BEAN_MAVEN_SERVICE= "releaser.mavenService";
    public static final String BEAN_SVN_SERVICE= "releaser.svnService";
    public static final String BEAN_GIT_MAVEN_PREPARE_UPDATE_REMOTE_REPOSITORY= "releaser.gitMavenPrepareUpdateRemoteRepository";
    public static final String BEAN_SVN_MAVEN_PREPARE_UPDATE_REMOTE_REPOSITORY= "releaser.svnMavenPrepareUpdateRemoteRepository";
   
    public static final String BEAN_COMPONENT_SERVICE= "releaser.componentService";
    
    public static final String BEAN_TWITTER_SERVICE= "releaser.twitterService";
    
    
    public static final String PROPERTY_ID_WORKFLOW_GIT_COMPONENT="releaser.idWorkflowGitComponent";
    public static final String PROPERTY_ID_WORKFLOW_SVN_COMPONENT="releaser.idWorkflowSvnComponent";
    public static final String PROPERTY_ID_WORKFLOW_LUTECE_SITE="releaser.idWorkflowLuteceSite";
    
    public static final String PROPERTY_LOCAL_SITE_BASE_PAH = "releaser.localSiteBasePath";
    public static final String PROPERTY_LOCAL_COMPONENT_BASE_PAH = "releaser.localComponentBasePath";
    
    public static final String PROPERTY_APPLICATION_ACCOUNT_ENABLE="releaser.applicationAccountEnable";
    public static final String PROPERTY_SVN_RELEASE_COMPONET_ACCOUNT_LOGIN= "releaser.svnReleaseComponetAccount.login";
    public static final String PROPERTY_SVN_RELEASE_COMPONET_ACCOUNT_PASSWORD="releaser.svnReleaseComponetAccount.password";
    public static final String PROPERTY_SITE_REPOSITORY_LOGIN= "releaser.site.repository.login";
    public static final String PROPERTY_SITE_REPOSITORY_PASSWORD= "releaser.site.repository.password";
    public static final String PROPERTY_GITHUB_RELEASE_COMPONET_ACCOUNT_LOGIN= "releaser.githubReleaseComponetAccount.login";
    public static final String PROPERTY_GITHUB_RELEASE_COMPONET_ACCOUNT_PASSWORD="releaser.githubReleaseComponetAccount.password";
    
    
    public static final String PROPERTY_MAVEN_LOCAL_REPOSITORY = "releaser.mavenLocalRepository";
    public static final String PROPERTY_MAVEN_HOME_PATH = "releaser.mavenHomePath";
    public static final String PROPERTY_PROXY_HOST="httpAccess.proxyHost";
    public static final String PROPERTY_PROXY_PORT="httpAccess.proxyPort";
    public static final String PROPERTY_TWITTER_OAUTH_CONSUMER_KEY="releaser.consumerKey";
    public static final String PROPERTY_TWITTER_OAUTH_CONSUMER_SECRET="releaser.consumerSecret";
    public static final String PROPERTY_TWITTER_OAUTH_ACCESS_TOKEN="releaser.accessToken";
    public static final String PROPERTY_TWITTER_OAUTH_ACCESS_TOKEN_SECRET="releaser.accessTokenSecret";
    public static final String PROPERTY_TWITTER_OAUTH_REQUEST_TOKEN_URL="releaser.requestTokenURL";
    public static final String PROPERTY_TWITTER_OAUTH_AUTHORIZATION_URL="releaser.authorizationURL";
    public static final String PROPERTY_TWITTER_OAUTH_ACCESSTOKEN_URL="releaser.accessTokenURL";
    public static final String PROPERTY_THREAD_RELEASE_POOL_MAX_SIZE="releaser.threadReleasePoolMaxSize";
    public static final String I18_TWITTER_MESSAGE="releaser.message.twitterMessage";
    public static final String PROPERTY_GROUP_ID_THEMES="releaser.groupIdThemes";
   
    
    
 
    
    
}
