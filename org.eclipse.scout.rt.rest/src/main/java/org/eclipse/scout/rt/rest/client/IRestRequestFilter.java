/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.client;

import javax.ws.rs.client.ClientRequestFilter;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Optional REST client request filter for all calls to the API.
 */
@Bean
public interface IRestRequestFilter extends ClientRequestFilter {
}
