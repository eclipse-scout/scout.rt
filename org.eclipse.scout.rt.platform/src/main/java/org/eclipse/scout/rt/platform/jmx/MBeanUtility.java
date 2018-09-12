/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.jmx;

import java.lang.management.ManagementFactory;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 8.0
 */
public final class MBeanUtility {
  private static final Logger LOG = LoggerFactory.getLogger(MBeanUtility.class);

  private MBeanUtility() {
    //static utility
  }

  public static ObjectName toJmxName(String domain, String type, String name) {
    try {
      return new ObjectName(domain + ":type=" + type + ",name=" + name);
    }
    catch (MalformedObjectNameException e) {
      throw new ProcessingException("Create ObjectName('', '', '')", domain, type, name, e);
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
