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

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;

/**
 * example strings.js:
 * 
 * <pre>
 * // not exported!
 * function _changeFirstLetter(string, funcName) {
 *   ...
 * };
 *
 * export function uppercaseFirstLetter(string) {
 *   ...
 * };
 *
 * export {
 *   ...,
 *   uppercaseFirstLetter,
 *   ...
 * };
 * </pre>
 */
public class T800_Utilities extends AbstractTask {
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"), PathFilters.isUtility());

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    // TODO MIG: write migration according the example in the comment
    MigrationUtility.prependTodo(workingCopy, "Migrate by hand");

  }
}
