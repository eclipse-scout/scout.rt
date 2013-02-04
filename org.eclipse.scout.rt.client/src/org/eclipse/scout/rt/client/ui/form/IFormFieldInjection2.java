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
package org.eclipse.scout.rt.client.ui.form;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * Interface extension to {@link IFormFieldInjection} that provides additional methods for filtering form fields before
 * they are initialized.
 * 
 * @since 3.8.2
 */
public interface IFormFieldInjection2 extends IFormFieldInjection {

  /**
   * @param container
   *          is the container field the given field classes are created for
   * @param fieldList
   *          live and mutable list of configured field classes (i.e. yet not instantiated)
   * @since 3.8.2
   */
  void filterFields(IFormField container, List<Class<? extends IFormField>> fieldList);
}
