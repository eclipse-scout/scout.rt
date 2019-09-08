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
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResolveReferencesAndCreateImportTask extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractResolveReferencesAndCreateImportTask.class);
  @SuppressWarnings("unchecked")
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"));

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  private String createImport(String toImport, String replacement, JsFile jsFile, Context context) {
    if (!StringUtility.hasText(toImport)) {
      return replacement;
    }

    // create import
    LOG.debug("[{}] Create import for '{}'.", jsFile.getPath(), toImport);
    String ref = jsFile.getOrCreateImport(toImport, context).getReferenceName();

    int firstDot = replacement.indexOf('.');
    String suffix = "";
    if (firstDot > 0) {
      suffix = replacement.substring(firstDot);
    }
    String importRef = ref + suffix;
    if (!importRef.equals(replacement)) {
      LOG.debug("modified import replacement from '{}' to '{}' because of import name clash.", replacement, importRef);
    }
    return importRef;
  }

  protected String createImportForReferences(String sourceFqn, String toImport, String replacement, String source, JsFile jsFile, Context context) {
    Matcher matcher = Pattern.compile("(?<!\\w)" + Pattern.quote(sourceFqn) + "(?!\\w)").matcher(source);
    boolean result = matcher.find();
    if (result) {
      String filename = jsFile.getPath().getFileName().toString();
      String importRef = createImport(toImport, replacement, jsFile, context);
      StringBuffer sb = new StringBuffer();
      // loop over all because of logging reasons
      do {
        LOG.debug("Reference replacement[{}]: '{}' -> '{}'", filename, matcher.group(), importRef);
        matcher.appendReplacement(sb, Matcher.quoteReplacement(importRef));
        result = matcher.find();
      }
      while (result);

      matcher.appendTail(sb);
      source = sb.toString();
    }

    return source;
  }

}
