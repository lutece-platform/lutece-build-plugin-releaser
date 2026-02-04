/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.releaser.util;

// TODO: Auto-generated Javadoc
/**
 * The Class ConstanteUtils.
 */
public class ConstanteUtils
{

    /** The Constant CONSTANTE_ID_NULL. */
    public static final int CONSTANTE_ID_NULL = -1;

    /** The Constant ERROR_TYPE_AUTHENTICATION_ERROR. */
    public static final String ERROR_TYPE_AUTHENTICATION_ERROR = "AUTHENTICATION_ERROR";

    /** The Constant CONSTANTE_SEPARATOR_SLASH. */
    public static final String CONSTANTE_SEPARATOR_SLASH = "/";

    /** The Constant CONSTANTE_TAGS. */
    public static final String CONSTANTE_TAGS = "tags";

    /** The Constant CONSTANTE_TRUNK. */
    public static final String CONSTANTE_TRUNK = "trunk";

    /** The Constant CONSTANTE_EMPTY_STRING. */
    public static final String CONSTANTE_EMPTY_STRING = "";

    /** The Constant CONSTANTE_MAX_RELEASE_CONTEXT_KEY. */
    public static final String CONSTANTE_MAX_RELEASE_CONTEXT_KEY = "max_release_context_key";

    /** The Constant CONSTANTE_RELEASE_CONTEXT_PREFIX. */
    public static final String CONSTANTE_RELEASE_CONTEXT_PREFIX = "release_context_";

    /** The Constant CONSTANTE_LAST_RELEASE_VERSION_PREFIX. */
    public static final String CONSTANTE_LAST_RELEASE_VERSION_PREFIX = "last_release_version_";

    /** The Constant CONSTANTE_LAST_RELEASE_NEXT_SNPASHOT_VERSION_PREFIX. */
    public static final String CONSTANTE_LAST_RELEASE_NEXT_SNPASHOT_VERSION_PREFIX = "last_release_next_snapshot_version_";

    /** The Constant CONSTANTE_COMPONENT_PROJECT_PREFIX. */
    public static final String CONSTANTE_COMPONENT_PROJECT_PREFIX = "component_project_prefix_";

    /** The Constant ATTRIBUTE_RELEASER_USER. */
    public static final String ATTRIBUTE_RELEASER_USER = "releaser_user";

    /** The Constant CONSTANTE_SUFFIX_GIT. */
    public static final String CONSTANTE_SUFFIX_GIT = "scm:git";

    /** The Constant CONSTANTE_POM_XML. */
    public static final String CONSTANTE_POM_XML = "pom.xml";

    /** The Constant BEAN_WORKFLOW_RELEASE_CONTEXT_SERVICE. */
    public static final String BEAN_WORKFLOW_RELEASE_CONTEXT_SERVICE = "releaser.workflowReleaseContextService";

    /** The Constant BEAN_MAVEN_SERVICE. */
    public static final String BEAN_MAVEN_SERVICE = "releaser.mavenService";

    /** The Constant BEAN_SVN_SERVICE. */
    public static final String BEAN_SVN_SERVICE = "releaser.svnService";

    /** The Constant BEAN_JIRA_SERVICE. */
    public static final String BEAN_JIRA_SERVICE = "releaser.jiraComponentService";

    /** The Constant BEAN_SVN_RESOURCE_SERVICE. */
    public static final String BEAN_SVN_RESOURCE_SERVICE = "releaser.cvs.svnResourceService";

    /** The Constant BEAN_GITHUB_RESOURCE_SERVICE. */
    public static final String BEAN_GITHUB_RESOURCE_SERVICE = "releaser.cvs.githubResourceService";

    /** The Constant BEAN_GITLAB_RESOURCE_SERVICE. */
    public static final String BEAN_GITLAB_RESOURCE_SERVICE = "releaser.cvs.gitlabResourceService";

    /** The Constant BEAN_COMPONENT_SERVICE. */
    public static final String BEAN_COMPONENT_SERVICE = "releaser.componentService";

    /** The Constant BEAN_TWITTER_SERVICE. */
    public static final String BEAN_TWITTER_SERVICE = "releaser.twitterService";

    /** The Constant BEAN_JENKINS_SERVICE. */
    public static final String BEAN_JENKINS_SERVICE = "releaser.jenkinsService";

    /** The Constant PROPERTY_POM_PARENT_SITE_VERSION. */
    public static final String PROPERTY_POM_PARENT_SITE_VERSION = "releaser.pomParentSiteVersion";

    /** The Constant PROPERTY_POM_PARENT_ARTIFCAT_ID. */
    public static final String PROPERTY_POM_PARENT_ARTIFCAT_ID = "releaser.pomParentArtifactId";

    /** The Constant PROPERTY_POM_PARENT_GROUP_ID. */
    public static final String PROPERTY_POM_PARENT_GROUP_ID = "releaser.pomParentGroupId";

    /** The Constant PROPERTY_ID_WORKFLOW_COMPONENT. */
    public static final String PROPERTY_ID_WORKFLOW_COMPONENT = "releaser.idWorkflowComponent";

    /** The Constant PROPERTY_ID_WORKFLOW_LUTECE_SITE. */
    public static final String PROPERTY_ID_WORKFLOW_LUTECE_SITE = "releaser.idWorkflowLuteceSite";

    /** The Constant PROPERTY_LOCAL_SITE_BASE_PAH. */
    public static final String PROPERTY_LOCAL_SITE_BASE_PAH = "releaser.localSiteBasePath";

    /** The Constant PROPERTY_LOCAL_COMPONENT_BASE_PAH. */
    public static final String PROPERTY_LOCAL_COMPONENT_BASE_PAH = "releaser.localComponentBasePath";

    /** The Constant PROPERTY_APPLICATION_ACCOUNT_ENABLE. */
    public static final String PROPERTY_APPLICATION_ACCOUNT_ENABLE = "releaser.applicationAccountEnable";

    /** The Constant PROPERTY_SVN_RELEASE_ACCOUNT_LOGIN. */
    public static final String PROPERTY_SVN_RELEASE_ACCOUNT_LOGIN = "releaser.svnReleaseAccount.login";

    /** The Constant PROPERTY_SVN_RELEASE_ACCOUNT_PASSWORD. */
    public static final String PROPERTY_SVN_RELEASE_ACCOUNT_PASSWORD = "releaser.svnReleaseAccount.password";

    /** The Constant PROPERTY_GITHUB_RELEASE_ACCOUNT_LOGIN. */
    public static final String PROPERTY_GITHUB_RELEASE_ACCOUNT_LOGIN = "releaser.githubReleaseAccount.login";

    /** The Constant PROPERTY_GITHUB_RELEASE_ACCOUNT_PASSWORD. */
    public static final String PROPERTY_GITHUB_RELEASE_ACCOUNT_PASSWORD = "releaser.githubReleaseAccount.password";

    /** The Constant PROPERTY_GITLAB_RELEASE_ACCOUNT_LOGIN. */
    public static final String PROPERTY_GITLAB_RELEASE_ACCOUNT_LOGIN = "releaser.gitlabReleaseAccount.login";

    /** The Constant PROPERTY_GITLAB_RELEASE_ACCOUNT_PASSWORD. */
    public static final String PROPERTY_GITLAB_RELEASE_ACCOUNT_PASSWORD = "releaser.gitlabReleaseAccount.password";

    /** The Constant PROPERTY_JENKINS_RELEASE_ACCOUNT_LOGIN. */
    public static final String PROPERTY_JENKINS_RELEASE_ACCOUNT_LOGIN = "releaser.jenkinsReleaseAccount.login";

    /** The Constant PROPERTY_JENKINS_RELEASE_ACCOUNT_PASSWORD. */
    public static final String PROPERTY_JENKINS_RELEASE_ACCOUNT_PASSWORD = "releaser.jenkinsReleaseAccount.password";

    /** The Constant PROPERTY_GITHUB_SEARCH_REPO_API. */
    public static final String PROPERTY_GITHUB_SEARCH_REPO_API = "releaser.githubSearchRepoApi";
    /** The Constant PROPERTY_NB_SEARCH_ITEM_PER_PAGE_LOAD. */
    public static final String PROPERTY_GITHUB_SEARCH_REPO_API_TOKEN = "releaser.githubSearchRepoApiToken";
    /** The Constant PROPERTY_MAVEN_LOCAL_REPOSITORY. */
    public static final String PROPERTY_MAVEN_LOCAL_REPOSITORY = "releaser.mavenLocalRepository";

    /** The Constant PROPERTY_MAVEN_PRIVATE_RELEASE_DEPLOYMENT_REPOSITORY. */
    public static final String PROPERTY_MAVEN_PRIVATE_RELEASE_DEPLOYMENT_REPOSITORY = "releaser.mavenPrivateReleaseDeploymentRepository";

    
    /** The Constant PROPERTY_MAVEN_HOME_PATH. */
    public static final String PROPERTY_MAVEN_HOME_PATH = "releaser.mavenHomePath";
    

    /** The Constant PROPERTY_PROXY_HOST. */
    public static final String PROPERTY_PROXY_HOST = "httpAccess.proxyHost";

    /** The Constant PROPERTY_PROXY_PORT. */
    public static final String PROPERTY_PROXY_PORT = "httpAccess.proxyPort";
    /** The Constant PROPERTY_PROXY_PORT. */
    public static final String PROPERTY_NO_PROXY_FOR_MAVEN="releaser.noProxyForMaven";

    /** The Constant PROPERTY_TWITTER_OAUTH_CONSUMER_KEY. */
    public static final String PROPERTY_TWITTER_OAUTH_CONSUMER_KEY = "releaser.consumerKey";

    /** The Constant PROPERTY_TWITTER_OAUTH_CONSUMER_SECRET. */
    public static final String PROPERTY_TWITTER_OAUTH_CONSUMER_SECRET = "releaser.consumerSecret";

    /** The Constant PROPERTY_TWITTER_OAUTH_ACCESS_TOKEN. */
    public static final String PROPERTY_TWITTER_OAUTH_ACCESS_TOKEN = "releaser.accessToken";

    /** The Constant PROPERTY_TWITTER_OAUTH_ACCESS_TOKEN_SECRET. */
    public static final String PROPERTY_TWITTER_OAUTH_ACCESS_TOKEN_SECRET = "releaser.accessTokenSecret";

    /** The Constant PROPERTY_TWITTER_OAUTH_REQUEST_TOKEN_URL. */
    public static final String PROPERTY_TWITTER_OAUTH_REQUEST_TOKEN_URL = "releaser.requestTokenURL";

    /** The Constant PROPERTY_TWITTER_OAUTH_AUTHORIZATION_URL. */
    public static final String PROPERTY_TWITTER_OAUTH_AUTHORIZATION_URL = "releaser.authorizationURL";

    /** The Constant PROPERTY_TWITTER_OAUTH_ACCESSTOKEN_URL. */
    public static final String PROPERTY_TWITTER_OAUTH_ACCESSTOKEN_URL = "releaser.accessTokenURL";

    /** The Constant PROPERTY_THREAD_RELEASE_POOL_MAX_SIZE. */
    public static final String PROPERTY_THREAD_RELEASE_POOL_MAX_SIZE = "releaser.threadReleasePoolMaxSize";

    /** The Constant PROPERTY_NB_SEARCH_ITEM_PER_PAGE_LOAD. */
    public static final String PROPERTY_NB_SEARCH_ITEM_PER_PAGE_LOAD = "releaser.nbSearchItemPerPageLoad";

    /** The Constant PROPERTY_URL_JIRA_SERVICE. */
    public static final String PROPERTY_URL_JIRA_SERVICE = "releaser.urlJiraService";

    /** The Constant PROPERTY_URL_JENKINS_SERVICE. */
    public static final String PROPERTY_URL_JENKINS_SERVICE = "releaser.urlJenkinsService";

    /** The Constant I18_TWITTER_MESSAGE. */
    public static final String I18_TWITTER_MESSAGE = "releaser.message.twitterMessage";

    /** The Constant CONSTANTE_TYPE_LUTECE_SITE. */
    public static final String CONSTANTE_TYPE_LUTECE_SITE = "lutece-site";

    /** The Constant CONSTANTE_GITHUB_ORG_LUTECE_SECTEUR_PUBLIC. */
    public static final String CONSTANTE_GITHUB_ORG_LUTECE_SECTEUR_PUBLIC = "lutece-secteur-public";

    /** The Constant CONSTANTE_GITHUB_ORG_LUTECE_PLATFORM. */
    public static final String CONSTANTE_GITHUB_ORG_LUTECE_PLATFORM = "lutece-platform";

    /** The Constant MARK_REPO_TYPE_GITHUB. */
    public static final String MARK_REPO_TYPE_GITHUB = "repo_type_github";

    /** The Constant MARK_REPO_TYPE_GITLAB. */
    public static final String MARK_REPO_TYPE_GITLAB = "repo_type_gitlab";

    /** The Constant MARK_REPO_TYPE_SVN. */
    public static final String MARK_REPO_TYPE_SVN = "repo_type_svn";

    /** The Constant MARK_USER. */
    public static final String MARK_USER = "user";
    
    /** The Constant PARAMETER_ACTION. */
    public static final String PARAMETER_ACTION = "action";    

    /** Minimum pom parent version to create docker image. */
    public static final String PROPERTY_POM_PARENT_MIN_VERSION_TO_CREATE_DOCKET_IMAGE = "releaser.pomParentMinVersionToCreateDockerImage";


}
