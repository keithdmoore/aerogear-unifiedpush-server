/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Stateless
@TransactionAttribute
@Path("/applications")
public class PushApplicationEndpoint extends AbstractBaseEndpoint {

    @Inject
    private PushApplicationService pushAppService;

    private static final Logger LOGGER = Logger.getLogger(PushApplicationEndpoint.class.getSimpleName());

    // CREATE
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerPushApplication(@Context HttpServletRequest request, PushApplication pushApp) {

        // some validation
        try {
            validateModelClass(pushApp);
        } catch (ConstraintViolationException cve) {

            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        // store the "developer:
        pushApp.setDeveloper(request.getUserPrincipal().getName());
        pushAppService.addPushApplication(pushApp);

        return Response.created(UriBuilder.fromResource(PushApplicationEndpoint.class).path(String.valueOf(pushApp.getPushApplicationID())).build()).entity(pushApp)
                .build();
    }

    // READ
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllPushApplications(@Context HttpServletRequest request) {
        LOGGER.info("===================================================");
        LOGGER.info(request.getUserPrincipal().getName());
        LOGGER.info("===================================================");
        return Response.ok(pushAppService.findAllPushApplicationsForDeveloper(request.getUserPrincipal().getName())).build();
    }

    @GET
    @Path("/{pushAppID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@Context HttpServletRequest request, @PathParam("pushAppID") String pushApplicationID) {

        PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, request.getUserPrincipal().getName());

        if (pushApp != null) {
            return Response.ok(pushApp).build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
    }

    // UPDATE
    @PUT
    @Path("/{pushAppID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePushApplication(@Context HttpServletRequest request, @PathParam("pushAppID") String pushApplicationID, PushApplication updatedPushApp) {

        PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, request.getUserPrincipal().getName());

        if (pushApp != null) {

            // some validation
            try {
                validateModelClass(updatedPushApp);
            } catch (ConstraintViolationException cve) {

                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            // update name/desc:
            pushApp.setDescription(updatedPushApp.getDescription());
            pushApp.setName(updatedPushApp.getName());
            pushAppService.updatePushApplication(pushApp);

            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
    }

    // UPDATE (MasterSecret Reset)
    @PUT
    @Path("/{pushAppID}/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetMasterSecret(@Context HttpServletRequest request, @PathParam("pushAppID") String pushApplicationID) {

        PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, request.getUserPrincipal().getName());

        if (pushApp != null) {
            // generate the new 'masterSecret' and apply it:
            String newMasterSecret = UUID.randomUUID().toString();
            pushApp.setMasterSecret(newMasterSecret);
            pushAppService.updatePushApplication(pushApp);

            return Response.ok(pushApp).build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
    }

    // DELETE
    @DELETE
    @Path("/{pushAppID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePushApplication(@Context HttpServletRequest request, @PathParam("pushAppID") String pushApplicationID) {

        PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, request.getUserPrincipal().getName());

        if (pushApp != null) {
            pushAppService.removePushApplication(pushApp);
            return Response.noContent().build();
        }
        return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
    }

    @GET
    @Path("/{pushAppID}/count")
    public Response countInstallations(@PathParam("pushAppID") String pushApplicationID) {
        Map<String, Long> result = pushAppService.countInstallationsByType(pushApplicationID);

        return Response.ok(result).build();
    }
}
