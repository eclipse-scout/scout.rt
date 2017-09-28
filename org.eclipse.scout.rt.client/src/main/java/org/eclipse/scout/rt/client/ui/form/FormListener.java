/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
