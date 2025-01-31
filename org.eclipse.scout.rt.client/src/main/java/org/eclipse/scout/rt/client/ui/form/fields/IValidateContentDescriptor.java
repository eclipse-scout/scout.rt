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

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * This interface is used to check fields for valid content and - in case invalid - activate / select / focus the
 * appropriate location
 * <p>
 * see {@link IFormField#validateContent()} and {@link IForm#validateForm()}
 */
public interface IValidateContentDescriptor {

  /**
   * Used to separate elements in a list of labels
   */
  String LABEL_SEPARATOR = " > ";

  /**
   * @return the name of the location/field/part
   */
  String getDisplayText();

  /**
   * Set the name of this descriptor. This is the name that will be displayed to the user.
   *
   * @param displayText
   *     The new text. May be {@code null}. In that case a default text is generated (based on location a label of
   *     the field).
   * @return this instance
   */
  IValidateContentDescriptor setDisplayText(String displayText);

  /**
   * @return The error status
   */
  IStatus getErrorStatus();

  /**
   * @return A {@link Consumer} that will be executed by the framework to show the problem location (jump to the
   * problem).
   */
  Consumer<IValidateContentDescriptor> getProblemLocationActivator();

  /**
   * Replaces the strategy to show the problem location (jump to the problem).
   *
   * @param locationActivator
   *     The new activator or {@code null}.
   * @return this instance
   */
  IValidateContentDescriptor setProblemLocationActivator(Consumer<IValidateContentDescriptor> locationActivator);

  /**
   * Adds the given {@link Consumer} to be executed before the currently active problem location activator
   * ({@link #getProblemLocationActivator()}). This replaces the current activator with a new one executing the given
   * consumer first and the existing one afterwards.
   *
   * @param before
   *     The additional task to execute before the existing one.
   * @return this instance
   */
  IValidateContentDescriptor problemLocationActivatorButBefore(Consumer<IValidateContentDescriptor> before);

  /**
   * Adds the given {@link Consumer} to be executed after the currently active problem location activator
   * ({@link #getProblemLocationActivator()}). This replaces the current activator with a new one executing the existing
   * consumer first and the given one afterwards.
   *
   * @param after
   *     The additional task to execute before the existing one.
   * @return this instance
   */
  IValidateContentDescriptor problemLocationActivatorAndThen(Consumer<? super IValidateContentDescriptor> after);

  /**
   * activate / select / focus the appropriate location.<br>
   * Executes the {@link Consumer} as set by {@link #setProblemLocationActivator(Consumer)},
   * {@link #problemLocationActivatorButBefore(Consumer)} or {@link #problemLocationActivatorAndThen(Consumer)}.<br>
   */
  void activateProblemLocation();
}
