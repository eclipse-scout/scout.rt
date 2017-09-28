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
package org.eclipse.scout.rt.platform.inventory;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.inventory.internal.JandexClassInventory;
import org.eclipse.scout.rt.platform.inventory.internal.JandexInventoryBuilder;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.jboss.jandex.IndexView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton with all the classes that were scanned in maven modules that contain /src/main/resources/META-INF/scout.xml
 */
public final class ClassInventory {

  private static final Logger LOG = LoggerFactory.getLogger(ClassInventory.class);
  private static final IClassInventory INSTANCE;

  public static IClassInventory get() {
    return INSTANCE;
  }

  static {
    try {
      long t0 = System.nanoTime();
      JandexInventoryBuilder inventoryBuilder = new JandexInventoryBuilder();
      if (LOG.isInfoEnabled()) {
        LOG.info("Building jandex class inventory using rebuild strategy {}...", inventoryBuilder.getRebuildStrategy());
      }
      inventoryBuilder.scanAllModules();
      IndexView index = inventoryBuilder.finish();
      long nanos = System.nanoTime() - t0;
      if (LOG.isInfoEnabled()) {
        LOG.info("Finished building jandex class inventory in {} ms. Total class count: {}", StringUtility.formatNanos(nanos), index.getKnownClasses().size());
      }
      INSTANCE = new JandexClassInventory(index);
    }
    catch (Exception t) {
      throw new PlatformException("Error while building class inventory", t);
    }
  }

  private ClassInventory() {
  }
}
