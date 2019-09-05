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
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsFunction;
import org.eclipse.scout.rt.platform.Order;

@Order(490)
public class T490_RemoveEndFunctionSemicolumns extends  AbstractTask {

  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"), PathFilters.isClass());

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    String source = workingCopy.getSource();
    JsFile jsFile = context.ensureJsFile(workingCopy);
    for (JsClass jsClass : jsFile.getJsClasses()) {
      source = processClass(jsClass,source);
    }
    workingCopy.setSource(source);
  }

  private String processClass(JsClass jsClass, String source) {
    for (JsFunction function : jsClass.getFunctions()) {
      if(!function.isMemoryOnly()){
        source = processFunction(function, source);
      }
    }
    return source;
  }

  private String processFunction(JsFunction function, String source) {
  return    source.replaceFirst("("+Pattern.quote(function.getSource())+")\\s*\\;", "$1");
  }
}
