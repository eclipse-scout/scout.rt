/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.client;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Marker-interface for "REST clients".
 * <p>
 * <b>Example:</b>
 *
 * <pre>
 * public class ExampleResourceClient implements IRestResourceClient {
 *
 *   protected static final String RESOURCE_PATH = "example";
 *
 *   protected MyRestClientHelper helper() {
 *     return BEANS.get(MyRestClientHelper.class);
 *   }
 *
 *   public ExampleDo loadExample(String id) {
 *     WebTarget target = helper().target(RESOURCE_PATH)
 *         .path("{id}/current")
 *         .resolveTemplate("id", id);
 *
 *     return target.request()
 *         .accept(MediaType.APPLICATION_JSON)
 *         .get(ExampleDo.class);
 *   }
 * }
 * </pre>
 *
 * <b>Usage:</b>
 *
 * <pre>
 * ExampleDo result = BEANS.get(ExampleResourceClient.class).loadExample(id);
 * </pre>
 */
@ApplicationScoped
public interface IRestResourceClient {
}
