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

import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.Order;

@Order(450)
public class T450_DeleteLibraries extends AbstractTask {

  Pattern JQUERY_PAT = Pattern.compile("webcontent[\\\\/]res[\\\\/]jquery-", Pattern.CASE_INSENSITIVE);
  Pattern JASMINE_PAT = Pattern.compile("webcontent[\\\\/]res[\\\\/]jasmine-", Pattern.CASE_INSENSITIVE);

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    String fullPath = pathInfo.getPath().toString();
    return JQUERY_PAT.matcher(fullPath).find() || JASMINE_PAT.matcher(fullPath).find();
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    context.ensureWorkingCopy(pathInfo.getPath()).setDeleted(true);
  }
}
