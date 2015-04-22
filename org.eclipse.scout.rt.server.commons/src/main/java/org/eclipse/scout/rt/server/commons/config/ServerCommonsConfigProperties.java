/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.config;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;

/**
 *
 */
public final class ServerCommonsConfigProperties {
  private ServerCommonsConfigProperties() {
  }

  /**
   * Expiration in milliseconds. Default is one hour.
   */
  public static class SessionCacheExpirationProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return Long.valueOf(TimeUnit.HOURS.toMillis(1));
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.commons.cache.AbstractHttpSessionCacheService#expiration";
    }
  }

  public static class ChainableFilterFailoverProperty extends AbstractFilterBooleanConfigProperty {

    @Override
    public Boolean getDefaultValue() {
      return Boolean.FALSE;
    }

    @Override
    public String getKey() {
      return "failover";
    }
  }

  public static class ChainableFilterRealmProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return "Default";
    }

    @Override
    public String getKey() {
      return "realm";
    }
  }

  public static class BasicFilterUsersProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "users";
    }
  }

  public static class LdapFilterServerProperty extends AbstractFilterStringConfigProperty {

    @Override
    protected IProcessingStatus getStatusRaw(String rawValue) {
      if (StringUtility.hasText(rawValue)) {
        return ProcessingStatus.OK_STATUS;
      }

      // mandatory
      return new ProcessingStatus("Property '" + getKey() + "' must be specified.", new Exception("origin"), 0, IProcessingStatus.ERROR);
    }

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "ldapServer";
    }
  }

  public static class LdapFilterBaseDnProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "ldapBaseDN";
    }
  }

  public static class LdapFilterGroupDnProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "lDAPgroupDN";
    }
  }

  public static class LdapFilterGroupAttributeIdProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "lDAPgroupAttributeId";
    }
  }

  public static class GzipFilterGetPatternProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return ".*\\.(html|css|js)";
    }

    @Override
    public String getKey() {
      return "get_pattern";
    }
  }

  public static class GzipFilterPostPatternProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return ".*/json";
    }

    @Override
    public String getKey() {
      return "post_pattern";
    }
  }

  public static class GzipFilterGetMinSizeProperty extends AbstractFilterIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return Integer.valueOf(64);
    }

    @Override
    public String getKey() {
      return "get_min_size";
    }
  }

  public static class GzipFilterPostMinSizeProperty extends AbstractFilterIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return Integer.valueOf(64);
    }

    @Override
    public String getKey() {
      return "post_min_size";
    }
  }

  public static class DataSourceFilterUseJndiProperty extends AbstractFilterBooleanConfigProperty {

    @Override
    public Boolean getDefaultValue() {
      return Boolean.FALSE;
    }

    @Override
    public String getKey() {
      return "useJndiConnection";
    }
  }

  public static class DataSourceFilterJndiInitialContextFactoryProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "jndiInitialContextFactory";
    }
  }

  public static class DataSourceFilterJndiProviderUrlProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "jndiProviderUrl";
    }
  }

  public static class DataSourceFilterJndiUrlPkgPrefixesProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "jndiUrlPkgPrefixes";
    }
  }

  public static class DataSourceFilterJndiNameProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "jndiName";
    }
  }

  public static class DataSourceFilterJdbcDriverNameProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "jdbcDriverName";
    }
  }

  public static class DataSourceFilterJdbcMappingNameProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "jdbcMappingName";
    }
  }

  public static class DataSourceFilterJdbcUsernameProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "jdbcUsername";
    }
  }

  public static class DataSourceFilterJdbcPasswordProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "jdbcPassword";
    }
  }

  public static class DataSourceFilterJdbcSelectUserPassProperty extends AbstractFilterStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "selectUserPass";
    }
  }
}
