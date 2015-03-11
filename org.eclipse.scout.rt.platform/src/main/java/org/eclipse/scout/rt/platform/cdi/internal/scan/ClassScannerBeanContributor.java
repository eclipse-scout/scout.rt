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
package org.eclipse.scout.rt.platform.cdi.internal.scan;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.Bean;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.IBeanContributor;

/**
 * auto find all {@link Bean} annotated classes
 */
public class ClassScannerBeanContributor implements IBeanContributor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClassScannerBeanContributor.class);

  @Override
  public void contributeBeans(IBeanContext context) {
    boolean preferJandex = true;
    AbstractBeanFinder scanner = null;
    try {
      //try jandex
      if (preferJandex) {
        scanner = new BeanFinderWithJandex();
      }
    }
    catch (Exception e) {
      LOG.info("org.jboss.jandex not available, using reflection to scan for @Bean annotated classes", e);
    }
    //use reflection
    if (scanner == null) {
      scanner = new BeanFinderWithReflection();
    }
    try {
      long t0 = System.nanoTime();
      scanner.scanAllModules();
      Collection<Class> classes = scanner.finish();
      long millis = (System.nanoTime() - t0) / 1000000L;

      LOG.info("detected " + classes.size() + " @Bean annotated classes in " + millis + "ms");

      for (Class<?> c : classes) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("detected @Bean " + c);
        }
        context.registerClass(c);
      }
    }
    catch (IOException ex) {
      LOG.error("scanning for @Bean", ex);
    }
  }

}
