package com.nextlabs.rms.services.resource;

import com.nextlabs.rms.config.GlobalConfigManager;
import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class GetAuthURLResource extends ServerResource {
    static final Logger logger = Logger.getLogger(GetAuthURLResource.class.getName());

    @Get
    public Representation doPost(Representation entity) {
        StringBuilder str = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        str.append("<AuthURL>");
        str.append(GlobalConfigManager.RMS_AUTH_URL);
        str.append("</AuthURL>");

        StringRepresentation response = new StringRepresentation(str, MediaType.TEXT_PLAIN);

        return response;
    }
}