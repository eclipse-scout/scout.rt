/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPortConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;

public final class ServerConfigProperties {
  private ServerConfigProperties() {
  }

  public static class ClusterSyncUserProperty extends AbstractStringConfigProperty {

    @Override
    protected String getDefaultValue() {
      return "system";
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.clustersync.ClusterSynchronizationService#user";
    }
  }

  /**
   * expiration for {@link ServerSessionProviderWithCache} in milliseconds. Default is one day.
   */
  public static class ServerSessionCacheExpirationProperty extends AbstractPositiveLongConfigProperty {

    @Override
    protected Long getDefaultValue() {
      return TimeUnit.DAYS.toMillis(1);
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache#expiration";
    }
  }

  public static class RemoteFilesRootDirProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.file.RemoteFileService#rootPath";
    }
  }

  public static class ImapHostProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.imap.AbstractIMAPService#host";
    }
  }

  public static class ImapPortProperty extends AbstractPortConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.imap.AbstractIMAPService#port";
    }
  }

  public static class ImapMailboxProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.imap.AbstractIMAPService#mailbox";
    }
  }

  public static class ImapUsernameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.imap.AbstractIMAPService#userName";
    }
  }

  public static class ImapPasswordProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.imap.AbstractIMAPService#password";
    }
  }

  public static class ImapSslProtocolsProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.imap.AbstractIMAPService#sslProtocols";
    }
  }

  public static class SmtpHostProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.smtp.AbstractSMTPService#host";
    }
  }

  public static class SmtpPortProperty extends AbstractPortConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.smtp.AbstractSMTPService#port";
    }
  }

  public static class SmtpUsernameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.smtp.AbstractSMTPService#username";
    }
  }

  public static class SmtpPasswordProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.smtp.AbstractSMTPService#password";
    }
  }

  public static class SmtpSubjectPrefixProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.smtp.AbstractSMTPService#subjectPrefix";
    }
  }

  public static class SmtpDefaultFromEmailProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.smtp.AbstractSMTPService#defaultFromEmail";
    }
  }

  public static class SmtpDebugReceiverEmailProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.smtp.AbstractSMTPService#debugReceiverEmail";
    }
  }

  public static class SmtpSslProtocolsProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.smtp.AbstractSMTPService#sslProtocols";
    }
  }

  public static class SmtpUseAuthenticationProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.smtp.AbstractSMTPService#useAuthentication";
    }
  }

  public static class SmtpUseSmtpsProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.services.common.smtp.AbstractSMTPService#useSmtps";
    }
  }

}
