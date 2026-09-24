/*
 * Copyright (c) 2002-2026, City of Paris
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
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
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
package fr.paris.lutece.plugins.releaser.util.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.paris.lutece.plugins.releaser.util.CommandResult;

/**
 * A release from tag must leave master and develop in a state where the next classic release merges back into master without conflict : the
 * stable tag is merged into master (no commit of its own on master) and recorded as integrated in develop (ours-merge).
 */
public class GitUtilsReleaseFromTagTest
{
    private static final String MASTER = "master";
    private static final String DEVELOP = "develop";
    private static final String POM = "pom.xml";
    private static final String PREPARE_RELEASE = "[maven-release-plugin] prepare release ";
    private static final String BETA_TAG = "component-1.0.0-beta-01";
    private static final String STABLE_TAG = "component-1.0.0";
    private static final String NEXT_TAG = "component-1.0.1";

    private File _bareDir;
    private File _workDir;
    private Git _git;

    /**
     * Creates a bare remote and a clone with master and develop on a common first commit.
     *
     * @throws Exception
     *             if the repositories cannot be created
     */
    @Before
    public void setUp( ) throws Exception
    {
        _bareDir = Files.createTempDirectory( "releaser-remote" ).toFile( );
        _workDir = Files.createTempDirectory( "releaser-work" ).toFile( );
        Git.init( ).setBare( true ).setDirectory( _bareDir ).call( ).close( );

        _git = Git.init( ).setDirectory( _workDir ).call( );
        _git.getRepository( ).getConfig( ).setString( "user", null, "name", "test" );
        _git.getRepository( ).getConfig( ).setString( "user", null, "email", "test@test" );
        _git.getRepository( ).getConfig( ).save( );
        _git.remoteAdd( ).setName( "origin" ).setUri( new URIish( _bareDir.toURI( ).toURL( ) ) ).call( );

        commitPom( "1.0.0-SNAPSHOT", "HEAD", "init" );
        _git.branchCreate( ).setName( DEVELOP ).call( );
        push( MASTER, DEVELOP );
    }

    /**
     * Deletes the temporary repositories.
     *
     * @throws IOException
     *             if a file cannot be deleted
     */
    @After
    public void tearDown( ) throws IOException
    {
        _git.close( );
        delete( _workDir );
        delete( _bareDir );
    }

    /**
     * Beta released the classic way, then the stable version released from the beta tag with the new flow, then the next classic release :
     * the merge back into master is clean and master carries the exact content of the new tag.
     *
     * @throws Exception
     *             if a git command fails
     */
    @Test
    public void testClassicReleaseAfterReleaseFromTag( ) throws Exception
    {
        _git.checkout( ).setName( DEVELOP ).call( );
        commitPom( "1.0.0-beta-01", BETA_TAG, PREPARE_RELEASE + BETA_TAG );
        _git.tag( ).setName( BETA_TAG ).setMessage( "beta" ).call( );
        commitPom( "1.0.0-SNAPSHOT", "HEAD", "[maven-release-plugin] prepare for next development iteration" );
        push( DEVELOP );
        _git.push( ).setPushTags( ).call( );
        _git.checkout( ).setName( DEVELOP ).call( );
        assertTrue( GitUtils.mergeBack( _git, DEVELOP, MASTER, "", "", commandResult( ) ).getMergeStatus( ).isSuccessful( ) );

        _git.checkout( ).setName( BETA_TAG ).call( );
        RevCommit stable = commitPom( "1.0.0", STABLE_TAG, "[release-from-tag] set version to 1.0.0 from " + BETA_TAG );
        _git.tag( ).setName( STABLE_TAG ).setMessage( "Release 1.0.0" ).call( );
        _git.push( ).setPushTags( ).call( );

        CommandResult masterResult = commandResult( );
        MergeResult intoMaster = GitUtils.mergeIntoMaster( _git, _git.getRepository( ).findRef( STABLE_TAG ), MASTER, "", "", masterResult );
        assertTrue( intoMaster.getMergeStatus( ).isSuccessful( ) );
        assertNull( masterResult.getError( ) );
        assertEquals( stable.getTree( ).getId( ), treeOf( MASTER ) );

        _git.checkout( ).setName( DEVELOP ).call( );
        commitPom( "1.0.1-SNAPSHOT", "HEAD", "[release-from-tag] bump snapshot to 1.0.1-SNAPSHOT after release 1.0.0" );
        ObjectId developBeforeOurs = _git.getRepository( ).resolve( DEVELOP );
        MergeResult ours = GitUtils.mergeOursStrategy( _git, STABLE_TAG, "[release-from-tag] mark " + STABLE_TAG + " as integrated in develop (ours)",
                commandResult( ) );
        assertTrue( ours.getMergeStatus( ).isSuccessful( ) );
        assertEquals( treeOf( developBeforeOurs ), treeOf( DEVELOP ) );
        assertTrue( readPom( ).contains( "<version>1.0.1-SNAPSHOT</version>" ) );
        push( DEVELOP );

        RevCommit next = commitPom( "1.0.1", NEXT_TAG, PREPARE_RELEASE + NEXT_TAG );
        _git.tag( ).setName( NEXT_TAG ).setMessage( "Release 1.0.1" ).call( );
        commitPom( "1.0.2-SNAPSHOT", "HEAD", "[maven-release-plugin] prepare for next development iteration" );
        push( DEVELOP );
        _git.push( ).setPushTags( ).call( );
        _git.checkout( ).setName( DEVELOP ).call( );

        CommandResult mergeBackResult = commandResult( );
        MergeResult mergeBack = GitUtils.mergeBack( _git, DEVELOP, MASTER, "", "", mergeBackResult );
        assertNotEquals( MergeResult.MergeStatus.CONFLICTING, mergeBack.getMergeStatus( ) );
        assertTrue( mergeBack.getMergeStatus( ).isSuccessful( ) );
        assertNull( mergeBackResult.getError( ) );
        assertEquals( next.getTree( ).getId( ), treeOf( MASTER ) );
        assertEquals( _git.getRepository( ).resolve( MASTER ), remoteHead( MASTER ) );
        assertTrue( readPom( ).contains( "<version>1.0.1</version>" ) );
    }

    /**
     * A command result with its log, as the workflow prepares it before a release.
     *
     * @return the command result
     */
    private static CommandResult commandResult( )
    {
        CommandResult commandResult = new CommandResult( );
        commandResult.setLog( new StringBuffer( ) );
        return commandResult;
    }

    /**
     * Writes the POM with a version and a scm tag, then commits it on the current branch.
     *
     * @param strVersion
     *            the version
     * @param strScmTag
     *            the scm tag
     * @param strMessage
     *            the commit message
     * @return the commit
     * @throws Exception
     *             if the commit fails
     */
    private RevCommit commitPom( String strVersion, String strScmTag, String strMessage ) throws Exception
    {
        String strPom = "<project>\n<version>" + strVersion + "</version>\n<scm><tag>" + strScmTag + "</tag></scm>\n</project>\n";
        Files.write( new File( _workDir, POM ).toPath( ), strPom.getBytes( StandardCharsets.UTF_8 ) );
        _git.add( ).addFilepattern( POM ).call( );
        return _git.commit( ).setMessage( strMessage ).call( );
    }

    /**
     * Pushes branches to the remote and refreshes the remote tracking refs.
     *
     * @param branches
     *            the branches
     * @throws GitAPIException
     *             if the push fails
     */
    private void push( String... branches ) throws GitAPIException
    {
        for ( String strBranch : branches )
        {
            _git.push( ).setRefSpecs( new RefSpec( "refs/heads/" + strBranch + ":refs/heads/" + strBranch ) ).call( );
        }
        _git.fetch( ).call( );
    }

    /**
     * Reads the POM of the work tree.
     *
     * @return the POM content
     * @throws IOException
     *             if the file cannot be read
     */
    private String readPom( ) throws IOException
    {
        return new String( Files.readAllBytes( new File( _workDir, POM ).toPath( ) ), StandardCharsets.UTF_8 );
    }

    /**
     * Head of a branch on the remote.
     *
     * @param strBranch
     *            the branch
     * @return the commit id
     * @throws IOException
     *             if the remote cannot be read
     */
    private ObjectId remoteHead( String strBranch ) throws IOException
    {
        try ( Git remote = Git.open( _bareDir ) )
        {
            return remote.getRepository( ).resolve( strBranch );
        }
    }

    /**
     * Tree at the head of a branch of the work repository.
     *
     * @param strBranch
     *            the branch
     * @return the tree id
     * @throws IOException
     *             if the repository cannot be read
     */
    private ObjectId treeOf( String strBranch ) throws IOException
    {
        return treeOf( _git.getRepository( ).resolve( strBranch ) );
    }

    /**
     * Tree of a commit of the work repository.
     *
     * @param commitId
     *            the commit id
     * @return the tree id
     * @throws IOException
     *             if the repository cannot be read
     */
    private ObjectId treeOf( ObjectId commitId ) throws IOException
    {
        Repository repository = _git.getRepository( );
        try ( RevWalk revWalk = new RevWalk( repository ) )
        {
            return revWalk.parseCommit( commitId ).getTree( ).getId( );
        }
    }

    /**
     * Deletes a directory recursively.
     *
     * @param file
     *            the file or directory
     * @throws IOException
     *             if a file cannot be deleted
     */
    private static void delete( File file ) throws IOException
    {
        if ( file.isDirectory( ) )
        {
            for ( File child : file.listFiles( ) )
            {
                delete( child );
            }
        }
        Files.deleteIfExists( file.toPath( ) );
    }
}
