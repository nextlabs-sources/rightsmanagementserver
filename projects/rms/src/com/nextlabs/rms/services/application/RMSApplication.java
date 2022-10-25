package com.nextlabs.rms.services.application;

import com.nextlabs.rms.services.resource.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class RMSApplication extends Application {
    static final Logger logger = LogManager.getLogger(RMSApplication.class.getName());

    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach("/RegisterAgent", RegisterAgentResource.class );
        router.attach("/CheckUpdates", CheckUpdatesResource.class );
        router.attach("/HeartBeat", HeartBeatResource.class);
        router.attach("/AckHeartBeat", AckHeartBeatResource.class);
        router.attach("/SendLog", LogResource.class );
        router.attach("/EvaluatePolicies",EvaluatePoliciesResource.class);
        router.attach("/ConvertFile",ConvertFileResource.class);
        router.attach("/Login",LoginResource.class);
        router.attach("/AddRepoService", AddRepoResource.class);
        router.attach("/UpdateRepoService", UpdateRepoResource.class);
        router.attach("/RemoveRepoService", RemoveRepoResource.class);
        router.attach("/GetRepositoryDetailsService", GetRepositoryDetailsResource.class);
        router.attach("/MarkFavoriteService", MarkFavoriteResource.class);
        router.attach("/UnMarkFavoriteService", UnMarkFavoriteResource.class);
        router.attach("/MarkOfflineService", MarkOfflineResource.class);
        router.attach("/UnMarkOfflineService", UnMarkOfflineResource.class);
        router.attach("/UserAttributesService", UserAttributesResource.class);
        router.attach("/GetAuthURL", GetAuthURLResource.class);
        return router;
    }
}