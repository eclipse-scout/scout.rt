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
package org.eclipse.scout.rt.client.ui.form.fields.filechooserfield;

import java.io.File;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.client.extension.ui.form.fields.filechooserfield.IFileChooserFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.filechooser.FileChooser;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;

@ClassId("8d2818c2-5659-4c03-87ef-09441302fbdd")
public abstract class AbstractFileChooserField extends AbstractValueField<String> implements IFileChooserField {

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

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(300)
  protected String getConfiguredFileIconId() {
    return AbstractIcons.FileChooserFieldFile;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(310)
  @ValidationRule(ValidationRule.MAX_LENGTH)
  protected int getConfiguredMaxLength() {
    return 4000;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setShowFileExtension(getConfiguredShowFileExtension());
    setFileExtensions(getConfiguredFileExtensions());
    setFileIconId(getConfiguredFileIconId());
    setMaxLength(getConfiguredMaxLength());
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
  public void setMaxLength(int len) {
    if (len > 0) {
      propertySupport.setPropertyInt(PROP_MAX_LENGTH, len);
    }
    if (isInitialized()) {
      setValue(getValue());
    }
  }

  @Override
  public int getMaxLength() {
    int len = propertySupport.getPropertyInt(PROP_MAX_LENGTH);
    if (len <= 0) {
      len = 200;
    }
    return len;
  }

  @Override
  public IFileChooser getFileChooser() {
    FileChooser fc = new FileChooser(getFileExtensions(), false);
    return fc;
  }

  // Convenience file getter
  // XXX BSH Refactor to BinaryResource!
  @Override
  public File getValueAsFile() {
    String value = getValue();
    if (value == null) {
      return null;
    }
    else {
      return new File(value);
    }
  }

  @Override
  public String getFileName() {
    File f = getValueAsFile();
    if (f != null) {
      return f.getName();
    }
    else {
      return null;
    }
  }

  @Override
  public long getFileSize() {
    File f = getValueAsFile();
    if (f != null) {
      return f.length();
    }
    else {
      return 0;
    }
  }

  @Override
  public boolean fileExists() {
    if (getValue() == null) {
      return false;
    }
    return getValueAsFile().exists();
  }

  // format value for display
  @Override
  protected String formatValueInternal(String validValue) {
    String s = validValue;
    if (s != null && s.length() > 0) {
      // XXX BSH Improve this
      String n = s;
      String e = "";
      if (n.indexOf('.') >= 0) {
        int i = n.lastIndexOf('.');
        e = n.substring(i);
        n = n.substring(0, i);
      }
      s = n;
      if (isShowFileExtension()) {
        s = s + e;
      }
    }
    return s;
  }

  @Override
  protected String parseValueInternal(String text) throws ProcessingException {
    // XXX BSH Test this
    String retVal = null;
    if (!StringUtility.hasText(text)) {
      return null;
    }
    text = text.trim();
    text = StringUtility.unquoteText(text);
    String n = text;
    String e = "";
    if (n.indexOf('.') >= 0) {
      int i = n.lastIndexOf('.');
      e = n.substring(i);
      n = n.substring(0, i);
    }
    text = n;
    if (e.length() == 0 && CollectionUtility.hasElements(m_fileExtensions)) {
      e = "." + CollectionUtility.firstElement(m_fileExtensions);
    }
    text += e;
    retVal = text;
    return retVal;
  }

  @Override
  protected String validateValueInternal(String text) throws ProcessingException {
    if (text != null && text.length() == 0) {
      text = null;
    }
    if (text != null) {
      if (text.length() > getMaxLength()) {
        text = text.substring(0, getMaxLength());
      }
    }
    return text;
  }

  private class P_UIFacade implements IFileChooserFieldUIFacade {

    @Override
    public void parseAndSetValueFromUI(String value) {
      if (value != null && value.length() == 0) {
        value = null;
      }
      // parse always, validity might change even if text is same
      parseAndSetValue(value);
    }

    @Override
    public void chooseFile() {
      try {
        IFileChooser fileChooser = getFileChooser();
        List<BinaryResource> result = fileChooser.startChooser();
        setValue(result.size() + " files"); // XXX
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  protected static class LocalFileChooserFieldExtension<OWNER extends AbstractFileChooserField> extends LocalValueFieldExtension<String, OWNER> implements IFileChooserFieldExtension<OWNER> {

    public LocalFileChooserFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IFileChooserFieldExtension<? extends AbstractFileChooserField> createLocalExtension() {
    return new LocalFileChooserFieldExtension<AbstractFileChooserField>(this);
  }

}
