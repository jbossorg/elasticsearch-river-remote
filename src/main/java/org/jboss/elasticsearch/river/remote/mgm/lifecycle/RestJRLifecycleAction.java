/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.remote.mgm.lifecycle;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.jboss.elasticsearch.river.remote.mgm.JRMgmBaseActionListener;
import org.jboss.elasticsearch.river.remote.mgm.RestJRMgmBaseAction;

import static org.elasticsearch.rest.RestStatus.OK;

/**
 * REST action handler for Remote river get state operation.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class RestJRLifecycleAction extends RestJRMgmBaseAction {

	@Inject
	protected RestJRLifecycleAction(Settings settings, Client client, RestController controller) {
		super(settings, controller, client);
		String baseUrl = baseRestMgmUrl();
		controller.registerHandler(org.elasticsearch.rest.RestRequest.Method.POST, baseUrl + "stop", this);
		controller.registerHandler(org.elasticsearch.rest.RestRequest.Method.POST, baseUrl + "restart", this);
	}

	@Override
	public void handleRequest(final RestRequest restRequest, final RestChannel restChannel, Client client) {

		JRLifecycleCommand command = JRLifecycleCommand.RESTART;
		if (restRequest.path().endsWith("stop"))
			command = JRLifecycleCommand.STOP;

		JRLifecycleRequest actionRequest = new JRLifecycleRequest(restRequest.param("riverName"), command);

		client
				.admin()
				.cluster()
				.execute(
						JRLifecycleAction.INSTANCE,
						actionRequest,
						new JRMgmBaseActionListener<JRLifecycleRequest, JRLifecycleResponse, NodeJRLifecycleResponse>(
								actionRequest, restRequest, restChannel) {

							@Override
							protected void handleRiverResponse(NodeJRLifecycleResponse nodeInfo) throws Exception {
								restChannel.sendResponse(new BytesRestResponse(OK, buildMessageDocument(restRequest,
										"Command successful")));
							}

						});
	}
}
