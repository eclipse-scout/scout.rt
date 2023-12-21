/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client;

import jakarta.ws.rs.client.ClientResponseFilter;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Global REST client response filter added to all APIs and invoked for <strong>every</strong> REST call.
 * <p>
 * When you implement a REST client helper for a specific purpose you should not implement this interface but use
 * <code>ClientResponseFilter</code> instead.
 */
@Bean
public interface IGlobalRestResponseFilter extends ClientResponseFilter {
}
