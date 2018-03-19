/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.client;

import javax.ws.rs.client.ClientRequestFilter;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Global REST client request filter added to all APIs and invoked for every REST call.
 */
@Bean
public interface IGlobalRestRequestFilter extends ClientRequestFilter {
}
