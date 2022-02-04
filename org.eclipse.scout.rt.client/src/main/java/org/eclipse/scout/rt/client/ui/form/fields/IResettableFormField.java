/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;

/**
 * only used in {@link AbstractForm#doReset} to include IComposerField which has been moved to separate module
 */
public interface IResettableFormField extends IFormField {

  /**
   * set field value to initValue and clear all error flags
   */
  void resetValue();
}
