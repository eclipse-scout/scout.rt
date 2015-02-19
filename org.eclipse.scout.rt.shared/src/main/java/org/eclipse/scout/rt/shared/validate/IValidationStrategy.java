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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.shared.validate.checks.CodeValueCheck;
import org.eclipse.scout.rt.shared.validate.checks.IValidateCheck;
import org.eclipse.scout.rt.shared.validate.checks.LookupValueCheck;
import org.eclipse.scout.rt.shared.validate.checks.MaxLengthCheck;
import org.eclipse.scout.rt.shared.validate.checks.RegexMatchCheck;

/**
 * The {@link DefaultValidator} in the
 * DefaultTransactionDelegate
 * reads this annotation in order to perform central input/output validation.
 */
public interface IValidationStrategy {

  /**
   * @return true if the check is part of the validation strategy
   */
  boolean accept(IValidateCheck check);

  /**
   * Perform no checks on arguments of the annotated method.
   * Use this annotation on a service if you check the arguments yourself.
   * <p>
   * see {@link ValidationUtility}
   */
  public static class NO_CHECK implements IValidationStrategy {
    @Override
    public boolean accept(IValidateCheck check) {
      return false;
    }
  }

  /**
   * Only perform max length checks on the arguments of the annotated method
   */
  public static class QUERY implements IValidationStrategy {
    private Set<String> m_accept = new HashSet<String>(Arrays.asList(new String[]{
        MaxLengthCheck.ID,
        CodeValueCheck.ID,
        LookupValueCheck.ID,
        RegexMatchCheck.ID
        }));

    @Override
    public boolean accept(IValidateCheck check) {
      return m_accept.contains(check.getCheckId());
    }
  }

  /**
   * Perform all checks on the arguments of the annotated method
   */
  public static class PROCESS implements IValidationStrategy {

    @Override
    public boolean accept(IValidateCheck check) {
      return true;
    }
  }

}
