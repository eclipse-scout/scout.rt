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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;

public interface IProposalField2<VALUE> extends ISmartField2<VALUE> {

  String getValueAsString();

  void setValueAsString(String value); // FIXME [awe] 7.0 - SF2: das müssen wir loswerden? mit AHO diskutieren, ich verstehe inzwischen, dass dieses
  // ganze MixedCAFields zeugs nötig war weil es sonst bzgl. generics typen von value und lookup key compiler probleme gibt (der LOOKUP_KEY ist typisiert,
  // und im fall von proposal-field != dem typ vom VALUE (String))

  /**
   * {@link Boolean}
   */
  String PROP_AUTO_CLOSE_CHOOSER = "autoCloseChooser";

  /**
   * {@link Integer}
   *
   * @since 6.1
   */
  String PROP_MAX_LENGTH = "maxLength";

  /**
   * {@link Boolean}
   *
   * @since 6.1
   */
  String PROP_TRIM_TEXT_ON_VALIDATE = "trimText";

  /**
   * @since 6.0
   */
  void setAutoCloseChooser(boolean autoCloseChooser);

  /**
   * @since 6.0
   */
  boolean isAutoCloseChooser();

  /**
   * @param maxLength
   *          of the text in this field. Negative values are automatically converted to 0.
   * @since 6.1
   */
  void setMaxLength(int maxLength);

  /**
   * @return the maximum length of text, default is 4000
   * @since 6.1
   */
  int getMaxLength();

  /**
   * @param b
   *          true if the entered text should pass through {@link String#trim()}
   * @since 6.1
   */
  void setTrimText(boolean b);

  /**
   * @return true if {@link String#trim()} is applied to the text entered, default true
   * @since 6.1
   */
  boolean isTrimText();

}
