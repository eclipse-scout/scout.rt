/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.client;

import javax.ws.rs.client.ClientRequestFilter;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Global REST client request filter added to all APIs and invoked for <strong>every</strong> REST call.
 * <p>
 * When you implement a REST client helper for a specific purpose you should not implement this interface but use
 * <code>ClientRequestFilter</code> instead.
 */
@Bean
public interface IGlobalRestRequestFilter extends ClientRequestFilter {
}
