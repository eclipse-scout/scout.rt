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
package org.eclipse.scout.migration.ecma6.task.post;

import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.api.ApiWriter;
import org.eclipse.scout.rt.platform.Order;

@Order(10000)
public class T10000_WriteApi implements IPostMigrationTask {
  @Override
  public void execute(Context context) {
    ApiWriter writer = new ApiWriter();
    try {
      if (!Files.exists(context.getLibraryStoreFile())) {
        Files.createDirectories(context.getLibraryStoreFile().getParent());
      }
      writer.writeLibrary(context.getLibraryStoreFile(), context.getLibraryName(), context);
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }
}
