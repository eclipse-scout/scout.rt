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
import org.eclipse.scout.migration.ecma6.model.references.AliasedMember;
import org.eclipse.scout.migration.ecma6.model.references.JsImport;
import org.eclipse.scout.rt.platform.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(4900)
public class T4900_CreateJQueryImports extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T4900_CreateJQueryImports.class);

  private static final String JQUERY_MODULE_NAME = "jquery";

  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"));

  private static final Pattern JQUERY_MEMBER_PAT = Pattern.compile("\\$\\.[^(]*");
  private static final Pattern JQUERY_FUNCTION_PAT = Pattern.compile("\\$\\(");

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    JsFile jsFile = context.ensureJsFile(workingCopy);
    String source = workingCopy.getSource();

    Matcher matcher = JQUERY_MEMBER_PAT.matcher(source);
    boolean jqueryImportNecessary = matcher.find();
    if (jqueryImportNecessary) {
      LOG.debug("JQuery usage found in '{}'.", jsFile.getPath());
    }
    else {
      matcher = JQUERY_FUNCTION_PAT.matcher(source);
      jqueryImportNecessary = matcher.find();
      if (jqueryImportNecessary) {
        LOG.debug("JQuery function call found in '{}'.", jsFile.getPath());
      }
    }
    if (jqueryImportNecessary) {
      JsImport jqueryImport = jsFile.getImport(JQUERY_MODULE_NAME);
      if (jqueryImport == null) {
        jqueryImport = new JsImport(JQUERY_MODULE_NAME);
        jqueryImport.setDefaultMember(new AliasedMember("$"));
        jsFile.addImport(jqueryImport);
      }
    }
  }
}
