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
package org.eclipse.scout.rt.client.ui.form.fields.splitbox;

import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;

/**
 * <h3>ISplitBox</h3> ...
 * 
 * @since 1.0.9 16.07.2008
 */
public interface ISplitBox extends ICompositeField {
  String PROP_SPLITTER_ENABLED = "splitterEnabled";
  String PROP_SPLIT_HORIZONTAL = "splitHorizontal";
  String PROP_SPLITTER_POSITION = "splitterPosition";

  boolean isSplitHorizontal();

  void setSplitHorizontal(boolean horizontal);

  boolean isSplitterEnabled();

  void setSplitterEnabled(boolean enabled);

  /**
   * Relative position of the splitter in the range 0..1
   */
  double getSplitterPosition();

  /**
   * Relative position of the splitter in the range 0..1
   */
  void setSplitterPosition(double position);

  /**
   * @return true if the splitter position is to be cached by the ui
   */
  boolean isCacheSplitterPosition();

  void setCacheSplitterPosition(boolean b);

  /**
   * set the storeSplitterPositionPropertyName to store the splitter position ui side. all splitboxes with the same
   * positionPropertyName have the same position of the splitter.
   * 
   * @return null to avoid storing the location a String to store the location under the given name.
   */
  String getCacheSplitterPositionPropertyName();

  void setCacheSplitterPositionPropertyName(String propName);

  ISplitBoxUIFacade getUIFacade();
}
