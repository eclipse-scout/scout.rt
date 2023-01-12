/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

import java.util.EventListener;

/**
 * A form listener must implement the method {@link #formChanged}. Register a listener via
 * {@link IForm#addFormListener(FormListener)} in order to react to certain changes.
 */
@FunctionalInterface
public interface FormListener extends EventListener {
  /**
   * When implementing a form listener, you will get a {@link FormEvent}. Its type will tell you more about the event
   * you're getting.
   */
  void formChanged(FormEvent e);
}
