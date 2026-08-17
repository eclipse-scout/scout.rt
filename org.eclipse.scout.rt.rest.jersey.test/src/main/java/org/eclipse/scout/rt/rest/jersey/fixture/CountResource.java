/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.fixture;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.rest.IRestResource;

@Path("count")
public class CountResource implements IRestResource {

  protected final AtomicInteger m_counter = new AtomicInteger();

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public int getCount() {
    return m_counter.getAndIncrement();
  }
}
