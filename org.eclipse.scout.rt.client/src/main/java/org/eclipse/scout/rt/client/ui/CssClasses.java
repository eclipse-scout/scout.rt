/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui;

import org.eclipse.scout.rt.client.ui.action.menu.form.fields.AbstractFormFieldMenu;

/**
 * CSS classes that can be used by the model to give the UI some hint about the rendering or behavior of a specific
 * component.
 *
 * @see IStyleable
 */
public interface CssClasses {

  /**
   * Class that applies the same vertical padding to the .field as to the label (useful e.g. for HTML fields)
   */
  String BORDERLESS_FIELD_PADDING_Y = "borderless-field-padding-y";

  /**
   * Class that hides the mandatory indicator (useful e.g. inside a {@link AbstractFormFieldMenu})
   */
  String NO_MANDATORY_INDICATOR = "no-mandatory-indicator";

  /**
   * Class that for example removes the right padding of the root-group-box in a form.
   */
  String RIGHT_PADDING_INVISIBLE = "right-padding-invisible";

  /**
   * Class that removes the top padding of group boxes, even if <i>borderVisible</i> is {@code true}.
   */
  String TOP_PADDING_INVISIBLE = "top-padding-invisible";

  /**
   * Class that removes the bottom padding of group boxes, even if <i>borderVisible</i> is {@code true}.
   */
  String BOTTOM_PADDING_INVISIBLE = "bottom-padding-invisible";

  /**
   * Class that increases the size of the left margin by the width of the "[X]" part of a check box field. Useful to
   * align a field with the label of the check box.
   */
  String CHECKBOX_INDENTATION = "checkbox-indentation";

  /**
   * Class that increases the size of the left margin by the width of the "(X)" part of radio button field. Useful to
   * align a field with the label of the radio button.
   */
  String RADIOBUTTON_INDENTATION = "radiobutton-indentation";

  /**
   * Class that applies a larger font size for button icons.
   */
  String BUTTON_ICON_LARGE = "button-icon-large";
}
