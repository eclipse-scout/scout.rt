/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.fields.filechooserbutton;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.filechooserbutton.IFileChooserButtonExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@ClassId("20038f90-75fa-4796-8a08-a9417ae69c60")
public abstract class AbstractFileChooserButton extends AbstractValueField<BinaryResource> implements IFileChooserButton {

  public AbstractFileChooserButton() {
    this(true);
  }

  public AbstractFileChooserButton(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredShowFileExtension() {
    return true;
  }

  @ConfigProperty(ConfigProperty.FILE_EXTENSIONS)
  @Order(200)
  protected List<String> getConfiguredFileExtensions() {
    return null;
  }

  @ConfigProperty(ConfigProperty.LONG)
  @Order(300)
  protected long getConfiguredMaximumUploadSize() {
    return DEFAULT_MAXIMUM_UPLOAD_SIZE;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(400)
  protected String getConfiguredIconId() {
    return null;
  }

  @Override
  protected boolean getConfiguredLabelVisible() {
    return false;
  }

  @Override
  protected boolean getConfiguredFillHorizontal() {
    return false;
  }

  @Override
  protected boolean getConfiguredStatusVisible() {
    return false;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setFileExtensions(getConfiguredFileExtensions());
    setMaximumUploadSize(getConfiguredMaximumUploadSize());
    setIconId(getConfiguredIconId());
    setHtmlEnabled(getConfiguredHtmlEnabled());
  }

  @Override
  public void setFileExtensions(List<String> fileExtensions) {
    setProperty(PROP_FILE_EXTENSIONS, CollectionUtility.arrayListWithoutNullElements(fileExtensions));
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getFileExtensions() {
    return CollectionUtility.arrayList((List<String>) getProperty(PROP_FILE_EXTENSIONS));
  }

  @Override
  public void setMaximumUploadSize(long maximumUploadSize) {
    propertySupport.setPropertyLong(PROP_MAXIMUM_UPLOAD_SIZE, maximumUploadSize);
  }

  @Override
  public long getMaximumUploadSize() {
    return propertySupport.getPropertyLong(PROP_MAXIMUM_UPLOAD_SIZE);
  }

  @Override
  public String getIconId() {
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  @Override
  public void setIconId(String iconId) {
    propertySupport.setPropertyString(PROP_ICON_ID, iconId);
  }

  @Override
  public String getFileName() {
    BinaryResource value = getValue();
    if (value != null) {
      return value.getFilename();
    }
    return null;
  }

  @Override
  public int getFileSize() {
    BinaryResource value = getValue();
    if (value != null) {
      return value.getContentLength();
    }
    return 0;
  }

  protected boolean getConfiguredHtmlEnabled() {
    return false;
  }

  @Override
  public void setHtmlEnabled(boolean enabled) {
    propertySupport.setProperty(PROP_HTML_ENABLED, enabled);
  }

  @Override
  public boolean isHtmlEnabled() {
    return propertySupport.getPropertyBool(PROP_HTML_ENABLED);
  }

  protected static class LocalFileChooserButtonExtension<OWNER extends AbstractFileChooserButton> extends LocalValueFieldExtension<BinaryResource, OWNER> implements IFileChooserButtonExtension<OWNER> {

    public LocalFileChooserButtonExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IFileChooserButtonExtension<? extends AbstractFileChooserButton> createLocalExtension() {
    return new LocalFileChooserButtonExtension<>(this);
  }
}
