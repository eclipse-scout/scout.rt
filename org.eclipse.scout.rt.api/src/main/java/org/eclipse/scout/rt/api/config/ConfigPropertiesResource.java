/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.api.data.config.ConfigPropertyDo;
import org.eclipse.scout.rt.api.data.config.IApiExposedConfigPropertyContributor;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.rest.IRestResource;

/**
 * REST resource to retrieve exposed {@link IConfigProperty} instances.
 */
@Path("config-properties")
public class ConfigPropertiesResource implements IRestResource {
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ConfigPropertyDo> list() {
    Set<ConfigPropertyDo> configProperties = new HashSet<>();
    BEANS.all(IApiExposedConfigPropertyContributor.class).forEach(contributor -> contributor.contribute(configProperties));
    return configProperties;
  }
}
