/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
