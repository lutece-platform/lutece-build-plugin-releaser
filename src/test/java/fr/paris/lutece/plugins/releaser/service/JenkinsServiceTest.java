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
package fr.paris.lutece.plugins.releaser.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

/**
 * Parsing of the Jenkins REST answers, with fixed JSON samples
 */
public class JenkinsServiceTest
{
    /**
     * A queue item has no executable while waiting, then carries the build URL and number.
     *
     * @throws IOException
     *             on JSON error
     */
    @Test
    public void testParseQueuedBuild( ) throws IOException
    {
        assertNull( JenkinsService.parseQueuedBuild( null ) );
        assertNull( JenkinsService.parseQueuedBuild( "" ) );
        assertNull( JenkinsService.parseQueuedBuild( "{\"_class\":\"hudson.model.Queue$WaitingItem\",\"blocked\":false,\"why\":\"In the quiet period\"}" ) );

        JenkinsBuild build = JenkinsService.parseQueuedBuild(
                "{\"_class\":\"hudson.model.Queue$LeftItem\",\"executable\":{\"_class\":\"org.jenkinsci.plugins.workflow.job.WorkflowRun\",\"number\":42,\"url\":\"https://jenkins/job/lutece-release-platform-step/42/\"}}" );
        assertEquals( 42, build.getNumber( ) );
        assertEquals( "https://jenkins/job/lutece-release-platform-step/42/", build.getUrl( ) );
    }

    /**
     * A running build has no result ; a finished build has one.
     *
     * @throws IOException
     *             on JSON error
     */
    @Test
    public void testParseBuildResult( ) throws IOException
    {
        assertNull( JenkinsService.parseBuildResult( "{\"building\":true,\"result\":null,\"number\":42}" ) );
        assertEquals( "SUCCESS", JenkinsService.parseBuildResult( "{\"building\":false,\"result\":\"SUCCESS\",\"number\":42}" ) );
        assertEquals( "FAILURE", JenkinsService.parseBuildResult( "{\"building\":false,\"result\":\"FAILURE\"}" ) );
    }
}
