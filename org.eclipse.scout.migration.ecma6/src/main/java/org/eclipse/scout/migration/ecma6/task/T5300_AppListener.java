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

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.rt.platform.Order;

@Order(5300)
public class T5300_AppListener extends AbstractTask {

  @SuppressWarnings("unchecked")
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"));
  private static final Pattern APP_LISTENER_PAT = Pattern.compile("scout\\.addAppListener\\('(\\w+)',");

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    if (!m_filter.test(pathInfo)) {
      return false;
    }
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    return APP_LISTENER_PAT.matcher(workingCopy.getSource()).find();
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());

    JsFile js = context.ensureJsFile(workingCopy);
    String refName = js.getOrCreateImport("scout.App", context).getReferenceName();
    String newSource = APP_LISTENER_PAT.matcher(workingCopy.getSource()).replaceAll(refName + ".addListener('$1',");

    workingCopy.setSource(newSource);
  }
}
