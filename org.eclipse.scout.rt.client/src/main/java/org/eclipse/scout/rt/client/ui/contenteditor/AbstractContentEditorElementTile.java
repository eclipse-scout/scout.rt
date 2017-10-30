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

import org.eclipse.scout.rt.client.ui.tile.AbstractTile;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * @since 7.1
 */
@ClassId("e7a53e7e-c562-43dc-bced-13ae6a3a548d")
public abstract class AbstractContentEditorElementTile extends AbstractTile implements IContentEditorElementTile {

  public AbstractContentEditorElementTile() {
    super();
  }

  public AbstractContentEditorElementTile(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setLabel(getConfiguredLabel());
    setDescription(getConfiguredDescription());
    setIdentifier(getConfiguredIdentifier());
    setIconId(getConfiguredIconId());
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredLabel() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  protected String getConfiguredDescription() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(30)
  protected String getConfiguredIdentifier() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(40)
  protected String getConfiguredIconId() {
    return null;
  }

  @Override
  public String getLabel() {
    return propertySupport.getPropertyString(PROP_LABEL);
  }

  @Override
  public void setLabel(String label) {
    propertySupport.setProperty(PROP_LABEL, label);
  }

  @Override
  public String getDescription() {
    return propertySupport.getPropertyString(PROP_DESCRIPTION);
  }

  @Override
  public void setDescription(String description) {
    propertySupport.setProperty(PROP_DESCRIPTION, description);
  }

  @Override
  public String getContentElementDesignHtml() {
    return propertySupport.getPropertyString(PROP_CONTENT_ELEMENT_DESIGN_HTML);
  }

  @Override
  public void setContentElementDesignHtml(String contentElementDesignHtml) {
    propertySupport.setProperty(PROP_CONTENT_ELEMENT_DESIGN_HTML, contentElementDesignHtml);
  }

  @Override
  public String getIdentifier() {
    return propertySupport.getPropertyString(PROP_IDENTIFIER);
  }

  @Override
  public void setIdentifier(String identifier) {
    propertySupport.setProperty(PROP_IDENTIFIER, identifier);
  }

  @Override
  public String getIconId() {
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  @Override
  public void setIconId(String iconId) {
    propertySupport.setPropertyString(PROP_ICON_ID, iconId);
  }
}
