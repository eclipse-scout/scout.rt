/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import java.util.function.Consumer;

public abstract class AbstractValidateContentDescriptor implements IValidateContentDescriptor {

  private Consumer<IValidateContentDescriptor> m_problemLocationActivator;
  private String m_displayText;

  protected AbstractValidateContentDescriptor() {
    m_problemLocationActivator = desc -> activateProblemLocationDefault();
  }

  @Override
  public Consumer<IValidateContentDescriptor> getProblemLocationActivator() {
    return m_problemLocationActivator;
  }

  @Override
  public AbstractValidateContentDescriptor setProblemLocationActivator(Consumer<IValidateContentDescriptor> locationActivator) {
    m_problemLocationActivator = locationActivator;
    return this;
  }

  @Override
  public AbstractValidateContentDescriptor problemLocationActivatorButBefore(Consumer<IValidateContentDescriptor> before) {
    if (before == null) {
      return this;
    }
    Consumer<IValidateContentDescriptor> current = getProblemLocationActivator();
    if (current == null) {
      setProblemLocationActivator(before);
    }
    else {
      setProblemLocationActivator(before.andThen(current));
    }
    return this;
  }

  @Override
  public AbstractValidateContentDescriptor problemLocationActivatorAndThen(Consumer<? super IValidateContentDescriptor> after) {
    if (after == null) {
      return this;
    }

    Consumer<IValidateContentDescriptor> current = getProblemLocationActivator();
    if (current == null) {
      setProblemLocationActivator(after::accept);
    }
    else {
      setProblemLocationActivator(current.andThen(after));
    }
    return this;
  }

  @Override
  public String getDisplayText() {
    return m_displayText;
  }

  @Override
  public AbstractValidateContentDescriptor setDisplayText(String displayText) {
    m_displayText = displayText;
    return this;
  }

  /**
   * @see CompositeFieldUtility#selectAllParentTabsOf(IFormField)
   */
  protected void selectAllParentTabsOf(IFormField formField) {
    CompositeFieldUtility.selectAllParentTabsOf(formField);
  }

  /**
   * Implements the default logic for problem location activation
   */
  protected abstract void activateProblemLocationDefault();

  @Override
  public void activateProblemLocation() {
    Consumer<? super IValidateContentDescriptor> locationActivator = getProblemLocationActivator();
    if (locationActivator != null) {
      locationActivator.accept(this);
    }
  }
}
