/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.servicetunnel;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.TunnelToServer;

public class ServiceTunnelClientConfigProperties {

  public static class BackendUrlProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.backendUrl";
    }

    @Override
    public String description() {
      return String.format("The URL of the scout backend server (without any servlets).\n"
          + "Example: %s=http://localhost:8080\n"
          + "By default this property is null.", getKey());
    }
  }

  public static class CompressServiceTunnelRequestProperty extends AbstractBooleanConfigProperty {

    @Override
    public Boolean getDefaultValue() {
      return true;
    }

    @Override
    public String description() {
      return "Specifies if the service tunnel should use gzip compression for requests, default value: " + getDefaultValue();
    }

    @Override
    public String getKey() {
      return "scout.servicetunnel.compress";
    }
  }

  public static class CreateTunnelToServerBeansProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.createTunnelToServerBeans";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Specifies if the Scout platform should create proxy beans for interfaces annotated with '%s'. Calls to beans of such types are then tunneled to the Scout backend.\n"
          + "By default this property is enabled if the property '%s' is set.", TunnelToServer.class.getSimpleName(), BEANS.get(BackendUrlProperty.class).getKey());
    }

    @Override
    public Boolean getDefaultValue() {
      // if no backend url is set proxy instances will not be created by default
      return StringUtility.hasText(BEANS.get(BackendUrlProperty.class).getValue());
    }
  }
}
