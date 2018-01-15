/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.http;

import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;

/**
 * <p>
 * Configuration property to define the default {@link IHttpTransportFactory}.
 * </p>
 * <p>
 * If property is not set, the default is {@link ApacheHttpTransportFactory}.
 * </p>
 */
public class HttpTransportFactoryProperty extends AbstractClassConfigProperty<IHttpTransportFactory> {

  @Override
  public Class<? extends IHttpTransportFactory> getDefaultValue() {
    return ApacheHttpTransportFactory.class;
  }

  @Override
  @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
  public String description() {
    return String.format("Fully qualified class name of the HTTP transport factory the application uses. The class must implement '%s'.\n"
        + "By default '%s' is used.", IHttpTransportFactory.class.getName(), ApacheHttpTransportFactory.class.getName());
  }

  @Override
  public String getKey() {
    return "scout.http.transportFactory";
  }
}
