/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security.csp;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractMapConfigProperty;
import org.eclipse.scout.rt.platform.config.ConfigUtility;

public final class ContentSecurityPolicyConfigProperties {

  private ContentSecurityPolicyConfigProperties() {
  }

  public static class CspEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    public Boolean getDefaultValue() {
      return true;
    }

    @Override
    public String description() {
      return String.format("Enable or disable Content Security Policy (CSP) headers. The headers can be modified by replacing the bean '%s' or using the property '%s'.",
          ContentSecurityPolicy.class.getName(), BEANS.get(CspDirectiveProperty.class).getKey());
    }

    @Override
    public String getKey() {
      return "scout.cspEnabled";
    }
  }

  public static class CspDirectiveProperty extends AbstractMapConfigProperty {

    @Override
    public String getKey() {
      return "scout.cspDirective";
    }

    @Override
    public String description() {
      return String.format("""
              Configures Content Security Policy (CSP Version 2) directives. The value must be provided as a Map.
              See https://www.w3.org/TR/CSP2/ or https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/CSP for available directives and their meaning.
              The values of this property replace the Scout defaults which are configured in the bean '%s'.
              If the Scout application uses different html entry points, the CSP can configured individually: for a specific entry point using its name as suffix in the map key: scout.cspDirective[script-src#entrypoint.html]. Directives without entrypoint suffix are used for all entry points.
              To append to configured defaults use the config property '%s' instead.
              Example: scout.cspDirective[img-src]='self' https://media.example.com""",
          ConfigurableContentSecurityPolicy.class.getName(), BEANS.get(CspDirectiveAppendProperty.class).getKey());
    }
  }

  public static class CspDirectiveAppendProperty extends AbstractMapConfigProperty {

    @Override
    public String getKey() {
      return "scout.cspDirectiveAppend";
    }

    @Override
    public String description() {
      return String.format("""
              Configures additional Content Security Policy (CSP Version 2) directive expressions. The value must be provided as a Map.
              See https://www.w3.org/TR/CSP2/ or https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/CSP for available directives and their meaning.
              The values of this property are appended to the already existing defaults (e.g. as configured in the bean '%s' or in a config.properties file).
              If the Scout application uses different html entry points, the CSP can configured individually: for a specific entry point using its name as suffix in the map key: scout.cspDirectiveAppend[script-src#entrypoint.html]. Directives without entrypoint suffix are used for all entry points.
              This allows to e.g. specify your default properties in the config.properties file using property '%s' and afterward extending it depending on a specific environment with an environment variable using this property.
              Example: scout.cspDirectiveAppend[img-src]=https://additional.host.org""",
          ConfigurableContentSecurityPolicy.class.getName(), BEANS.get(CspDirectiveProperty.class).getKey());
    }
  }

  public static class CspExclusionsProperty extends AbstractConfigProperty<List<Pattern>, List<String>> {

    @Override
    public String getKey() {
      return "scout.cspExclusions";
    }

    @Override
    public List<String> readFromSource(String namespace) {
      return ConfigUtility.getPropertyList(getKey(), null, namespace);
    }

    @Override
    protected List<Pattern> parse(List<String> value) {
      if (value == null) {
        return null;
      }
      return value.stream()
          .filter(Objects::nonNull)
          .map(Pattern::compile)
          .collect(Collectors.toList());
    }

    @Override
    public String description() {
      return String.format("A list of regex strings. If the pathInfo of the request matches one of these strings the csp headers won't be set. This property only has an effect if csp is enabled, see '%s'.",
          BEANS.get(CspEnabledProperty.class).getKey());
    }
  }
}
