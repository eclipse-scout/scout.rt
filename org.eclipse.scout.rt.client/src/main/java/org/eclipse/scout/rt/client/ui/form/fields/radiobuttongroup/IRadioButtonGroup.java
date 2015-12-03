/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;

public interface IRadioButtonGroup<T> extends IValueField<T>, ICompositeField {

  /**
   * @return the buttons controlled by this radio button group
   */
  List<IRadioButton<T>> getButtons();

  /**
   * @return the button representing this value
   */
  IRadioButton<T> getButtonFor(T radioValue);

  /**
   * @return the selected radio button controlled by this radio button group
   */
  IRadioButton<T> getSelectedButton();

  /**
   * select a button controlled by this radio button group
   */
  void selectButton(IRadioButton<T> button);

  /**
   * @return the radio value of the selected button controlled by this radio button group
   */
  T getSelectedKey();

  /**
   * select the buttons controlled by this radio button group with this radio value
   */
  void selectKey(T key);

}
