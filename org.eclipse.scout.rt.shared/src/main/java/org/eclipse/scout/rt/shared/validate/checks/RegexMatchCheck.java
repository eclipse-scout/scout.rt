/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.validate.checks;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.shared.validate.ValidationUtility;
import org.eclipse.scout.rt.shared.validate.annotations.RegexMatch;

/**
 * Check implementation of {@link RegexMatch} annotation for method parameter or field.
 */
public class RegexMatchCheck implements IValidateCheck {
  public static final String ID = "regexMatch";

  private Pattern m_pat;

  public RegexMatchCheck(String pattern) {
    this(Pattern.compile(pattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE));
  }

  public RegexMatchCheck(Pattern pat) {
    m_pat = pat;
  }

  @Override
  public String getCheckId() {
    return ID;
  }

  @Override
  public boolean accept(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj instanceof String) {
      return true;
    }
    if (obj.getClass() == String[].class) {
      return true;
    }
    return false;
  }

  @Override
  public void check(Object obj) throws Exception {
    if (obj.getClass().isArray()) {
      ValidationUtility.checkRegexMatchArray(obj, m_pat);
    }
    else {
      ValidationUtility.checkRegexMatchValue(obj, m_pat);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + m_pat.pattern();
  }
}
