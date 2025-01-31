/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Helper for resolving local host name and address.
 */
@ApplicationScoped
public class LocalHostAddressHelper {

  public static final String UNKNOWN = "unknown";

  /**
   * Instead of just throwing an UnknownHostException and giving up, this method grabs a suitable hostname from the
   * exception and prevents the exception from being thrown. If a suitable hostname cannot be acquired from the
   * exception {@link #UNKNOWN} is returned.
   *
   * @return The local hostname or {@value #UNKNOWN} if it cannot be determined.
   * @see InetAddress#getLocalHost()
   * @see InetAddress#getHostName()
   * @see #parseUnknownHostException(UnknownHostException)
   */
  @SuppressWarnings("squid:S1166") // catch without rethrow
  public String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException e) {
      return parseUnknownHostException(e);
    }
  }

  /**
   * When using the {@link InetAddress#getHostName()} method in an environment where neither a proper DNS
   * lookup nor an <tt>/etc/hosts</tt> entry exists for a given host, the following exception will be thrown: <code>
   * java.net.UnknownHostException: &lt;hostname&gt;: &lt;hostname&gt;
   * at java.net.InetAddress.getLocalHost(InetAddress.java:1425)
   * ...
   * </code>
   * <p>
   * This method tries to parse the hostname out of the exception and returns {@value #UNKNOWN} if no hostname can be
   * found.
   */
  protected String parseUnknownHostException(UnknownHostException exception) {
    if (exception != null && exception.getMessage() != null) {
      String message = exception.getMessage(); // Format: "hostname: hostname"
      int colon = StringUtility.indexOf(message, ":");
      if (colon > 0) {
        return message.substring(0, colon);
      }
    }
    return UNKNOWN;
  }

  /**
   * @return The local host IP address or {@value #UNKNOWN} if the local IP address cannot be determined.
   */
  @SuppressWarnings("squid:S1166") // catch without rethrow
  public String getHostAddress() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    }
    catch (UnknownHostException e) {
      return UNKNOWN;
    }
  }
}
