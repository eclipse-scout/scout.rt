/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6;

import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Migration implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(Migration.class);

  protected Migration() {
  }

  public static void main(String[] args) {
    Migration migration = new Migration();
    migration.run();
  }

  @Override
  public void run() {
    new MigrationStep1().run();
    if (Configuration.get().getSourceModuleDirectory().equals(Configuration.get().getTargetModuleDirectory())) {
      if (!MigrationUtility.waitForUserConfirmation()) {
        return;
      }
    }
    new MigrationStep2().run();
    LOG.info("Migration completed.");
  }
}
