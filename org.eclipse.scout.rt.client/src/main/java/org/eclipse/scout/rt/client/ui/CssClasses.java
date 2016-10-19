/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.client.ui;

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
   * Marker class that prevents the focus manager from setting the initial focus (when the form is opened) to the
   * component or one of its children. Useful for HTML fields that contain links.
   */
  String PREVENT_INITIAL_FOCUS = "prevent-initial-focus";
}
