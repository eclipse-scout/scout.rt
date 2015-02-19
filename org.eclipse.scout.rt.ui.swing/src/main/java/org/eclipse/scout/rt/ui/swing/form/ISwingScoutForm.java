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
package org.eclipse.scout.rt.ui.swing.form;

import javax.swing.JComponent;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.basic.ISwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutView;

public interface ISwingScoutForm extends ISwingScoutComposite<IForm> {

  JComponent getSwingFormPane();

  void setInitialFocus();

  /**
   * @return the view which is currently displaying this form
   */
  ISwingScoutView getView();

  /**
   * This method can (doesn't need to) be called when a form is to be detached
   * from its view but the view is kept open. For example wizard views may use
   * this method to display a series of forms one after another.
   * {@link ISwingEnvironment#createForm(ISwingScoutView, IForm)} to attach {@link ISwingScoutForm#detachSwingView()} to
   * detach
   */
  void detachSwingView();

}
