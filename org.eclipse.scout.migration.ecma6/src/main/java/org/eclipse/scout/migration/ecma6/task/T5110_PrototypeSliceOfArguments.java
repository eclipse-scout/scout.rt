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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.Order;

/**
 * <pre>
 *   Replace
 *     Array.prototype.slice.call(arguments, N)
 *   by ES6 spread
 *     [...arguments].slice(N)
 * </pre>
 */
@Order(5110)
public class T5110_PrototypeSliceOfArguments extends AbstractTask {

  private static final Pattern PROTOYPE_SLICE_PAT = Pattern.compile("Array\\.prototype\\.slice\\.call\\(arguments,?\\s*");
  private Path m_relativeNamespaceDirectory;

  @Override
  public void setup(Context context) {
    if (Configuration.get().getJsFolderName() == null) {
      return;
    }
    m_relativeNamespaceDirectory = Paths.get("src", "main", "js", Configuration.get().getJsFolderName());
  }

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    if (m_relativeNamespaceDirectory == null) {
      return false;
    }
    return pathInfo.getModuleRelativePath().startsWith(m_relativeNamespaceDirectory);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy wc = context.ensureWorkingCopy(pathInfo.getPath());
    String source = wc.getSource();
    source = PROTOYPE_SLICE_PAT.matcher(source).replaceAll(Matcher.quoteReplacement("[...arguments].slice("));
    wc.setSource(source);
  }
}
