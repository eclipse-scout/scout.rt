/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.test;

import java.util.Date;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ThreadContext;
import org.osgi.framework.Bundle;

public final class ServerTestUtility {

  private ServerTestUtility() {
  }

  public static void sleep(final int seconds) {
    try {
      Thread.sleep(1000L * seconds);
    }
    catch (InterruptedException e) {
    }
  }

  public static String getNowAsString() {
    return "" + new Date();
  }

  public static Date getNowAsDate() {
    return new Date();
  }

  public static IServerSession getServerSession() {
    return ThreadContext.getServerSession();
  }

  public static Bundle getServerBundle() {
    return ThreadContext.getServerSession().getBundle();
  }

}
