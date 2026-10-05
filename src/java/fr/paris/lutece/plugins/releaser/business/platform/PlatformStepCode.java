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
package fr.paris.lutece.plugins.releaser.business.platform;

/**
 * The five steps of a platform release and their fixed behaviour. What may vary (repository URL, label) lives in {@link PlatformStepDefinition}.
 */
public enum PlatformStepCode
{
    /** Step 1 : lutece-global-pom and its build tools. Stable only. */
    GLOBAL_POM( 1, ComponentSource.REFERENCED_ARTIFACTS, true, true, Executor.STEP_PIPELINE ),
    /** Step 2 : lutece-site-pom, parent and technical components updated from step 1. Stable only. */
    SITE_POM( 2, ComponentSource.NONE, true, true, Executor.STEP_PIPELINE ),
    /** Step 3 : lutece-core and its libraries. */
    CORE( 3, ComponentSource.REFERENCED_ARTIFACTS, false, true, Executor.STEP_PIPELINE ),
    /** Step 4 : the plugins of lutece-platform, driven by the lutece.*.version properties, then the export. */
    PLATFORM_PLUGINS( 4, ComponentSource.VERSION_PROPERTIES, false, false, Executor.STEP_PIPELINE ),
    /** Step 5 : the starters and the BOM of lutece-platform, through the existing release pipeline of the monorepo. */
    PLATFORM_STARTERS( 5, ComponentSource.NONE, false, false, Executor.PLATFORM_PIPELINE );

    /**
     * Where the components of a step are read in the aggregate pom.
     */
    public enum ComponentSource
    {
        /** No component : only the aggregate is released. */
        NONE,
        /** Dependencies, managed dependencies, build plugins and managed plugins filtered on the Lutece groupId prefix. */
        REFERENCED_ARTIFACTS,
        /** Version properties (lutece.artifactId.version), groupId resolved through the bill of materials. */
        VERSION_PROPERTIES
    }

    /**
     * Which Jenkins job executes the step.
     */
    public enum Executor
    {
        /** The generic step pipeline : components in order, then the aggregate. */
        STEP_PIPELINE,
        /** The existing release pipeline of lutece-platform. */
        PLATFORM_PIPELINE
    }

    /** The step number. */
    private final int _nStepNumber;

    /** The component source. */
    private final ComponentSource _componentSource;

    /** Whether the aggregate can only be released as a stable version. */
    private final boolean _bStableOnly;

    /** Whether the aggregate follows the lutece-core branch of its core version (develop7.x) rather than the plugins one (develop_core7). */
    private final boolean _bCoreLineBranch;

    /** The executor. */
    private final Executor _executor;

    /**
     * Constructor.
     *
     * @param nStepNumber
     *            the step number
     * @param componentSource
     *            the component source
     * @param bStableOnly
     *            true if the aggregate is released as a stable version only
     * @param bCoreLineBranch
     *            true if the aggregate follows the lutece-core branch of its core version
     * @param executor
     *            the executor
     */
    PlatformStepCode( int nStepNumber, ComponentSource componentSource, boolean bStableOnly, boolean bCoreLineBranch, Executor executor )
    {
        _nStepNumber = nStepNumber;
        _componentSource = componentSource;
        _bStableOnly = bStableOnly;
        _bCoreLineBranch = bCoreLineBranch;
        _executor = executor;
    }

    /**
     * Returns the step number.
     *
     * @return the step number (1..5)
     */
    public int getStepNumber( )
    {
        return _nStepNumber;
    }

    /**
     * Returns the component source.
     *
     * @return the component source
     */
    public ComponentSource getComponentSource( )
    {
        return _componentSource;
    }

    /**
     * Tells whether the aggregate can only be released as a stable version.
     *
     * @return true for global-pom and site-pom
     */
    public boolean isStableOnly( )
    {
        return _bStableOnly;
    }

    /**
     * Tells whether the aggregate follows the lutece-core branch of its core version.
     *
     * @return true for global-pom, site-pom and core
     */
    public boolean isCoreLineBranch( )
    {
        return _bCoreLineBranch;
    }

    /**
     * Tells whether the step releases its aggregate. The plugins step only updates the version properties of the lutece-platform POM, the
     * monorepo itself is released by the last step.
     *
     * @return false for the plugins step
     */
    public boolean isAggregateReleased( )
    {
        return this != PLATFORM_PLUGINS;
    }

    /**
     * Returns the executor.
     *
     * @return the executor
     */
    public Executor getExecutor( )
    {
        return _executor;
    }

    /**
     * Returns the number of steps.
     *
     * @return the number of steps
     */
    public static int getStepCount( )
    {
        return values( ).length;
    }

    /**
     * Returns the code of a step number.
     *
     * @param nStepNumber
     *            the step number
     * @return the code, or null if the number is out of range
     */
    public static PlatformStepCode fromStepNumber( int nStepNumber )
    {
        for ( PlatformStepCode code : values( ) )
        {
            if ( code.getStepNumber( ) == nStepNumber )
            {
                return code;
            }
        }

        return null;
    }
}
