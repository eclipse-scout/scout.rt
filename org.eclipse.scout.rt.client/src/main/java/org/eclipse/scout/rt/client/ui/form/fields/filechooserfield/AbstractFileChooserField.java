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
package org.eclipse.scout.rt.client.ui.form.fields.filechooserfield;

import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.filechooserfield.IFileChooserFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

@ClassId("8d2818c2-5659-4c03-87ef-09441302fbdd")
public abstract class AbstractFileChooserField extends AbstractValueField<BinaryResource> implements IFileChooserField {

  private IFileChooserFieldUIFacade m_uiFacade;

  private boolean m_showFileExtension;
  private List<String> m_fileExtensions;

  public AbstractFileChooserField() {
    this(true);
  }

  public AbstractFileChooserField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  protected boolean getConfiguredShowFileExtension() {
    return true;
  }

  @ConfigProperty(ConfigProperty.FILE_EXTENSIONS)
  @Order(230)
  protected List<String> getConfiguredFileExtensions() {
    return null;
  }

  @ConfigProperty(ConfigProperty.LONG)
  @Order(310)
  protected long getConfiguredMaximumUploadSize() {
    return DEFAULT_MAXIMUM_UPLOAD_SIZE;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();
    setShowFileExtension(getConfiguredShowFileExtension());
    setFileExtensions(getConfiguredFileExtensions());
    setMaximumUploadSize(getConfiguredMaximumUploadSize());
  }

  @Override
  public IFileChooserFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void setShowFileExtension(boolean b) {
    m_showFileExtension = b;
    refreshDisplayText();
  }

  @Override
  public boolean isShowFileExtension() {
    return m_showFileExtension;
  }

  @Override
  public void setFileExtensions(List<String> a) {
    m_fileExtensions = CollectionUtility.arrayListWithoutNullElements(a);
  }

  @Override
  public List<String> getFileExtensions() {
    return CollectionUtility.arrayList(m_fileExtensions);
  }

  @Override
  public void setFileIconId(String s) {
    propertySupport.setPropertyString(PROP_FILE_ICON_ID, s);
  }

  @Override
  public String getFileIconId() {
    return propertySupport.getPropertyString(PROP_FILE_ICON_ID);
  }

  @Override
  public void setMaximumUploadSize(long maximumUploadSize) {
    propertySupport.setPropertyLong(PROP_MAXIMUM_UPLOAD_SIZE, maximumUploadSize);
  }

  @Override
  public long getMaximumUploadSize() {
    return propertySupport.getPropertyInt(PROP_MAXIMUM_UPLOAD_SIZE);
  }

  // Convenience file getter

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

  // format value for display
  @Override
  protected String formatValueInternal(BinaryResource validValue) {
    if (validValue == null) {
      return null;
    }
    String filename = validValue.getFilename();
    if (StringUtility.hasText(filename)) {
      if (!isShowFileExtension() && filename.indexOf('.') >= 0) {
        return filename.substring(0, filename.lastIndexOf("."));
      }
      return filename;
    }
    return null;
  }

  @Override
  protected BinaryResource parseValueInternal(String text) {
    // Don't allow to edit the value - except to completely delete it!
    if (StringUtility.hasText(text)) {
      return getValue();
    }
    return null;
  }

  protected class P_UIFacade implements IFileChooserFieldUIFacade {

    @Override
    public void parseAndSetValueFromUI(String value) {
      if (!isEnabled() || !isVisible()) {
        return;
      }
      parseAndSetValue(value);
    }
  }

  protected static class LocalFileChooserFieldExtension<OWNER extends AbstractFileChooserField> extends LocalValueFieldExtension<BinaryResource, OWNER> implements IFileChooserFieldExtension<OWNER> {

    public LocalFileChooserFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IFileChooserFieldExtension<? extends AbstractFileChooserField> createLocalExtension() {
    return new LocalFileChooserFieldExtension<AbstractFileChooserField>(this);
  }
}
