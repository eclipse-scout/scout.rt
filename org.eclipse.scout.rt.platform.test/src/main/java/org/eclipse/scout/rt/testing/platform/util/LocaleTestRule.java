/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util;

import java.util.Locale;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.testing.platform.runner.SafeStatementInvoker;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test rule to use a fixed locale.
 */
public class LocaleTestRule implements TestRule {

  private Locale m_locale;

  public LocaleTestRule(Locale locale) {
    m_locale = locale;
  }

  public Locale getLocale() {
    return m_locale;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        SafeStatementInvoker invoker = new SafeStatementInvoker(base);
        RunContexts.copyCurrent().withLocale(getLocale()).run(invoker);
        invoker.throwOnError();
      }
    };
  }
}
