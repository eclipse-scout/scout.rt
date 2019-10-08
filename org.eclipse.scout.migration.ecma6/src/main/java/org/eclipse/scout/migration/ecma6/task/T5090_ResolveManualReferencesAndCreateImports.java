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
package org.eclipse.scout.migration.ecma6.task;

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.rt.platform.Order;

@Order(5090)
public class T5090_ResolveManualReferencesAndCreateImports extends AbstractResolveReferencesAndCreateImportTask {

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());

    String source = workingCopy.getSource();
    JsFile jsFile = context.ensureJsFile(workingCopy);

    /*
     * <pre>
     * scout.app -> App.get()
     * scout.sessions -> App.get().sessions
     * scout.errorHandler -> App.get().errorHandler
     * </pre>
     */
    source = createImportForReferences("scout.app", "scout.App", "App.get()", source, jsFile, context);
    source = createImportForReferences("scout.sessions", "scout.App", "App.get().sessions", source, jsFile, context);
    source = createImportForReferences("scout.errorHandler", "scout.App", "App.get().errorHandler", source, jsFile, context);

    workingCopy.setSource(source);
  }
}
