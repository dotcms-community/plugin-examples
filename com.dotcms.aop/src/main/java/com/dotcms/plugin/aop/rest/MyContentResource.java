package com.dotcms.plugin.aop.rest;

import com.dotcms.rest.InitDataObject;
import com.dotcms.rest.ResponseEntityView;
import com.dotcms.rest.WebResource;
import com.dotcms.rest.annotation.NoCache;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.liferay.portal.model.User;
import org.glassfish.jersey.server.JSONP;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/custom/content")
public class MyContentResource {

    private final WebResource webResource = new WebResource();
    private final MyContentService contentService = new MyContentService();
    private final ContentletAPI contentletAPI = APILocator.getContentletAPI();

    @GET
    @Path("/inode/{inode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContentByInode(@Context final HttpServletRequest request,
                                      @Context final HttpServletResponse response,
                               @PathParam("inode") final String inode) throws DotSecurityException, DotDataException {

        final InitDataObject initData = this.webResource.init
                (request, response, false);
        final User user = initData.getUser();
        final Contentlet contentlet = this.contentService.hydrateContentLet
                    (this.contentletAPI.find(inode, user, true));

        return Response.ok(new ResponseEntityView (contentlet)).build();

    }

    @Path("/lastMonth")
    @DELETE
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public Response deleteLastMonthContent(@Context final HttpServletRequest request,
                                           @Context final HttpServletResponse response) throws DotDataException {

        this.webResource.init
                (request, response, false);
        return Response.ok(new ResponseEntityView
                    (this.contentService.deleteLastMonthContent())).build();
    }
}