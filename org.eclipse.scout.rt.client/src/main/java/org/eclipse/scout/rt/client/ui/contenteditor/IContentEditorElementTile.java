/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.contenteditor;

import org.eclipse.scout.rt.client.ui.tile.ITile;

/**
 * @since 7.1
 */
public interface IContentEditorElementTile extends ITile {

  String PROP_LABEL = "label";
  String PROP_CONTENT_ELEMENT_DESIGN_HTML = "contentElementDesignHtml";
  String PROP_IDENTIFIER = "identifier";
  String PROP_ICON_ID = "iconId";
  String PROP_DESCRIPTION = "description";

  String getLabel();

  void setLabel(String label);

  String getDescription();

  void setDescription(String description);

  String getContentElementDesignHtml();

  void setContentElementDesignHtml(String contentElementDesignHtml);

  String getIdentifier();

  void setIdentifier(String identifier);

  String getIconId();

  void setIconId(String iconId);
}
