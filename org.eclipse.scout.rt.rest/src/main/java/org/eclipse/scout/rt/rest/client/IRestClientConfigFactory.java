/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.client;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Factory for creating JAX-RS implementor-specific {@link Configuration} objects that are used to build new
 * {@link ClientBuilder} instances.
 *
 * @see ClientBuilder#withConfig(Configuration)
 */
@ApplicationScoped
public interface IRestClientConfigFactory {

  /**
   * @return new JAX-RS implementor-specific {@link Configuration}.
   */
  Configuration createClientConfig();
}
