/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.jmx;

import java.lang.management.ManagementFactory;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scout convention is to use the MBEAN java package base name as MBean domain and the MBean main type as MBean name.
 *
 * @since 8.0
 */
public final class MBeanUtility {
  private static final Logger LOG = LoggerFactory.getLogger(MBeanUtility.class);

  private MBeanUtility() {
    //static utility
  }

  /**
   * @since 9.0
   */
  public static ObjectName toJmxName(String domain, String name) {
    return toJmxName(domain, null, name);
  }

  /**
   * @param domain
   *          Scout convention is to use the package base name
   * @param name
   *          Scout convention is to use the MBean main type simple name
   */
  public static ObjectName toJmxName(String domain, String type, String name) {
    try {
      if (StringUtility.isNullOrEmpty(type)) {
        return new ObjectName(domain + ":name=" + name);
      }
      else {
        return new ObjectName(domain + ":type=" + type + ",name=" + name);
      }
    }
    catch (MalformedObjectNameException e) {
      throw new ProcessingException("Create ObjectName('{}', '{}', '{}')", domain, type, name, e);
    }
  }

  public static void register(ObjectName name, Object monitor) {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      if (mbs.isRegistered(name)) {
        mbs.unregisterMBean(name);
      }
      mbs.registerMBean(monitor, name);
    }
    catch (JMException e) {
      LOG.warn("Could not register MBean '{}'", name, e);
    }
  }

  public static void unregister(ObjectName name) {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      if (mbs.isRegistered(name)) {
        mbs.unregisterMBean(name);
      }
    }
    catch (JMException e) {
      LOG.warn("Could not unregister MBean '{}'", name, e);
    }
  }
}
