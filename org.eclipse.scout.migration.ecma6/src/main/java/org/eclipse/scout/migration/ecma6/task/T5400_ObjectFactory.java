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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.rt.platform.Order;

@Order(5400)
public class T5400_ObjectFactory extends AbstractTask {

  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"));
  private static final Pattern OBJECT_FACTORY_PAT = Pattern.compile("scout\\.objectFactory([.;])");

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    Matcher matcher = OBJECT_FACTORY_PAT.matcher(workingCopy.getSource());
    if (matcher.find()) {
      JsFile js = context.ensureJsFile(workingCopy);
      String refName = js.getOrCreateImport("scout.ObjectFactory", context).getReferenceName();
      String newSource = matcher.replaceAll(refName + ".get()$1");
      workingCopy.setSource(newSource);
    }
  }
}
