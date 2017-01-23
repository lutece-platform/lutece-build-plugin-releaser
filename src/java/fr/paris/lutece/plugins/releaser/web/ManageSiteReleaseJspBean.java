/*
 * Copyright (c) 2002-2016, Mairie de Paris
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
package fr.paris.lutece.plugins.releaser.web;

import fr.paris.lutece.plugins.releaser.business.Site;
import fr.paris.lutece.plugins.releaser.service.SiteService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.html.HtmlTemplate;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * ManageSiteRelease JSP Bean abstract class for JSP Bean
 */
@Controller( controllerJsp = "ManageSiteRelease.jsp", controllerPath = "jsp/admin/plugins/releaser/", right = "RELEASER_SITE_MANAGEMENT" )
public class ManageSiteReleaseJspBean extends MVCAdminJspBean
{
    private static final String PARAMETER_SITE_ID = "id_site";
    private static final String VIEW_MANAGE_SITE_RELEASE = "siteRelease";
    private static final String TEMPLATE_PREPARE_SITE_RELEASE = "/admin/plugins/releaser/prepare_site_release.html";
    private static final String MARK_SITE = "site";
    private Site _site;

    @View( value = VIEW_MANAGE_SITE_RELEASE, defaultView = true )
    public String getPrepareSiteRelease( HttpServletRequest request )
    {
        int nSiteId = 0;
        try
        {
            nSiteId = Integer.parseInt( request.getParameter( PARAMETER_SITE_ID ) );
        }
        catch( NumberFormatException e )
        {
            // TODO
        }
        _site = SiteService.getSite( nSiteId );
        Map<String, Object> model = getModel( );
        model.put( MARK_SITE, _site );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_PREPARE_SITE_RELEASE, getLocale( ), model );
        return template.getHtml( );
    }
}
