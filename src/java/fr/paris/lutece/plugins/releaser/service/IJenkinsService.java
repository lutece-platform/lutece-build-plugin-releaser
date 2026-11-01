package fr.paris.lutece.plugins.releaser.service;

import java.io.IOException;
import java.util.Map;

import fr.paris.lutece.plugins.releaser.business.WorkflowReleaseContext;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

/**
 * Jenkins integration : triggers jobs, follows their builds and reads their artifacts.
 */
public interface IJenkinsService
{
    /**
     * Loads the Jenkins configuration.
     */
    public abstract void init( );

    /**
     * Triggers the configured docker image pipeline for a site and waits for its result, logging in the release console.
     *
     * @param context
     *            the release context
     * @return the build result (SUCCESS, FAILURE...), null on error
     */
    String TriggerPipeline( WorkflowReleaseContext context );

    /**
     * Triggers a parameterized job.
     *
     * @param strJobPath
     *            the job path relative to the Jenkins base URL (ex : {@code job/lutece-release-platform-step})
     * @param mapParams
     *            the build parameters
     * @return the URL of the queue item
     * @throws HttpAccessException
     *             if Jenkins cannot be reached or refuses the request
     */
    String triggerJob( String strJobPath, Map<String, String> mapParams ) throws HttpAccessException;

    /**
     * Waits until a queued item becomes a build.
     *
     * @param strQueueUrl
     *            the URL of the queue item
     * @return the build
     * @throws HttpAccessException
     *             if Jenkins cannot be reached
     * @throws IOException
     *             if the Jenkins answer cannot be read
     * @throws InterruptedException
     *             if the wait is interrupted
     */
    JenkinsBuild waitForBuild( String strQueueUrl ) throws HttpAccessException, IOException, InterruptedException;

    /**
     * Reads a queue item once.
     *
     * @param strQueueUrl
     *            the URL of the queue item
     * @return the build once Jenkins has started it, null while the item is still queued
     * @throws HttpAccessException
     *             if Jenkins cannot be reached
     * @throws QueueItemCancelledException
     *             if the item was cancelled before it started : no build will ever run
     * @throws IOException
     *             if the Jenkins answer cannot be read
     */
    JenkinsBuild getQueuedBuild( String strQueueUrl ) throws HttpAccessException, IOException;

    /**
     * A queue item cancelled before it started : no build will run.
     */
    final class QueueItemCancelledException extends IOException
    {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param strMessage
         *            the message
         */
        public QueueItemCancelledException( String strMessage )
        {
            super( strMessage );
        }
    }

    /**
     * Returns the result of a build.
     *
     * @param strBuildUrl
     *            the build URL
     * @return the result (SUCCESS, FAILURE, UNSTABLE, ABORTED), null while the build is running
     * @throws HttpAccessException
     *             if Jenkins cannot be reached
     * @throws IOException
     *             if the Jenkins answer cannot be read
     */
    String getBuildResult( String strBuildUrl ) throws HttpAccessException, IOException;

    /**
     * Reads an artifact archived by a build.
     *
     * @param strBuildUrl
     *            the build URL
     * @param strArtifactPath
     *            the artifact path relative to the workspace (ex : {@code step-report.json})
     * @return the artifact content
     * @throws HttpAccessException
     *             if Jenkins cannot be reached or the artifact does not exist
     */
    String readArtifact( String strBuildUrl, String strArtifactPath ) throws HttpAccessException;
}
