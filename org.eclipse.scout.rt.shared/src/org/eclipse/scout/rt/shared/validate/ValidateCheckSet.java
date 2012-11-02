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
package org.eclipse.scout.rt.shared.validate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.scout.rt.shared.validate.checks.IValidateCheck;

/**
 * Container for checks on specific object and subtree
 * <p>
 * Checks are applied iff no excplicit checks are found. The checks are evaluated from right to left (last added checks
 * before first added checks). When a check with the same id has already been run (accepted), all others with same class
 * are ignored.
 */
public class ValidateCheckSet {
  private final List<IValidateCheck> m_checks;
  private ValidateCheckSet m_parent;

  public ValidateCheckSet() {
    m_checks = new ArrayList<IValidateCheck>();
  }

  public ValidateCheckSet getParent() {
    return m_parent;
  }

  public void setParent(ValidateCheckSet parent) {
    m_parent = parent;
  }

  public boolean isEmpty() {
    return m_checks.isEmpty();
  }

  public void addCheck(IValidateCheck check) {
    if (check != null) {
      m_checks.add(check);
    }
  }

  public void applyChecks(IValidationStrategy strategy, Object obj, HashSet<String> consumedChecks) throws Exception {
    if (isEmpty() && m_parent == null) {
      return;
    }
    for (ListIterator<IValidateCheck> it = m_checks.listIterator(m_checks.size()); it.hasPrevious();) {
      IValidateCheck check = it.previous();
      String id = check.getCheckId();
      if (consumedChecks.contains(id)) {
        continue;
      }
      if (!strategy.accept(check)) {
        continue;
      }
      if (check.accept(obj)) {
        consumedChecks.add(id);
        check.check(obj);
      }
    }
    if (m_parent != null) {
      m_parent.applyChecks(strategy, obj, consumedChecks);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + m_checks + ", parent=" + m_parent;
  }
}
