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
package org.eclipse.scout.rt.server;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPortConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.server.services.common.file.RemoteFileService;

public final class ServerConfigProperties {

  private ServerConfigProperties() {
  }

  public static class ClusterSyncUserProperty extends AbstractStringConfigProperty {

    public static final String CLUSTER_SYNC_USER_NAME = "system";

    @Override
    public String getDefaultValue() {
      return CLUSTER_SYNC_USER_NAME;
    }

    @Override
    public String getKey() {
      return "scout.clustersync.user";
    }

    @Override
    public String description() {
      return String.format("Technical subject under which received cluster sync notifications are executed. The default value is '%s'.", CLUSTER_SYNC_USER_NAME);
    }
  }

  public static class ServerSessionCacheExpirationProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return TimeUnit.DAYS.toMillis(1);
    }

    @Override
    public String getKey() {
      return "scout.serverSessionTtl";
    }

    @Override
    public String description() {
      return "Server sessions that have not been accessed for the specified number of milliseconds are removed from the cache. The default value is one day.";
    }
  }

  public static class RemoteFilesRootDirProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.remotefileRootPath";
    }

    @Override
    public String description() {
      return String.format("Absolute path to the root directory of the '%s'. The default value is null.", RemoteFileService.class.getSimpleName());
    }
  }

  public static class ImapHostProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.imap.host";
    }

    @Override
    public String description() {
      return "The IMAP server to connect to.";
    }
  }

  public static class ImapPortProperty extends AbstractPortConfigProperty {

    @Override
    public String getKey() {
      return "scout.imap.port";
    }

    @Override
    public String description() {
      return "The port on which the IMAP server should be contacted. If not specified, the default port is used.";
    }
  }

  public static class ImapMailboxProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.imap.mailbox";
    }

    @Override
    public String description() {
      return "The mailbox folder name to open. If not specified the default folder is opened.";
    }
  }

  public static class ImapUsernameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.imap.username";
    }

    @Override
    public String description() {
      return "Default user name for IMAP.";
    }
  }

  public static class ImapPasswordProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.imap.password";
    }

    @Override
    public String description() {
      return "The password to connect to the mailbox.";
    }
  }

  public static class ImapSslProtocolsProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.imap.sslProtocols";
    }

    @Override
    public String description() {
      return "Specifies the SSL protocols that will be enabled for SSL connections. The property value is a whitespace separated list of tokens acceptable to the javax.net.ssl.SSLSocket.setEnabledProtocols() method.";
    }
  }

  public static class SmtpHostProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.smtp.host";
    }

    @Override
    public String description() {
      return "SMTP server host name.";
    }
  }

  public static class SmtpPortProperty extends AbstractPortConfigProperty {

    @Override
    public String getKey() {
      return "scout.smtp.port";
    }

    @Override
    public String description() {
      return "The port to connect to the server.";
    }
  }

  public static class SmtpUsernameProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.smtp.username";
    }

    @Override
    public String description() {
      return "SMTP server username.";
    }
  }

  public static class SmtpPasswordProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.smtp.password";
    }

    @Override
    public String description() {
      return "SMTP server password";
    }
  }

  public static class SmtpSubjectPrefixProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.smtp.subjectPrefix";
    }

    @Override
    public String description() {
      return "Text that will be added in front of each email subject that is sent. The default value is null.";
    }
  }

  public static class SmtpDefaultFromEmailProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.smtp.defaultFromEmail";
    }

    @Override
    public String description() {
      return "Default sender Email address.";
    }
  }

  public static class SmtpDebugReceiverEmailProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.smtp.debugReceiverEmail";
    }

    @Override
    public String description() {
      return "If specified all emails are sent to this address instead of the real one. This may be useful during development to not send emails to real users by accident.";
    }
  }

  public static class SmtpSslProtocolsProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.smtp.sslProtocols";
    }

    @Override
    public String description() {
      return "Specifies the SSL protocols that will be enabled for SSL connections. The property value is a whitespace separated list of tokens acceptable to the javax.net.ssl.SSLSocket.setEnabledProtocols() method.";
    }
  }

  public static class SmtpUseAuthenticationProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.smtp.useAuth";
    }

    @Override
    public String description() {
      return "If true, attempt to authenticate the user using the AUTH command.";
    }
  }

  public static class SmtpUseSmtpsProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.smtp.useSsl";
    }

    @Override
    public String description() {
      return "Specifies if a secure connection should be used.";
    }
  }
}
