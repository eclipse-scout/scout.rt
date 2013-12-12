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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.filechooser.FileChooser;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

public abstract class AbstractFileChooserField extends AbstractValueField<String> implements IFileChooserField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractFileChooserField.class);

  private File m_directory;
  private String[] m_fileExtensions;
  private boolean m_typeLoad;
  private boolean m_folderMode;
  private boolean m_showDirectory;
  private boolean m_showFileName;
  private boolean m_showFileExtension;
  private IMenu[] m_menus;
  private IFileChooserFieldUIFacade m_uiFacade;

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
  @Order(240)
  protected boolean getConfiguredFolderMode() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(250)
  protected boolean getConfiguredShowDirectory() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(260)
  protected boolean getConfiguredShowFileName() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  protected boolean getConfiguredShowFileExtension() {
    return true;
  }

  /**
   * Load or Save
   * <ul>
   * <li><code>true</code> loads the file from the file system into the application.</li>
   * <li><code>false</code> saves the file from the application to the file system. Attention: This does not work in
   * RAP/Web-UI</li>
   * </ul>
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(280)
  protected boolean getConfiguredTypeLoad() {
    return false;
  }

  @ConfigProperty(ConfigProperty.FILE_EXTENSIONS)
  @Order(230)
  protected String[] getConfiguredFileExtensions() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(290)
  protected String getConfiguredDirectory() {
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

  private Class<? extends IMenu>[] getConfiguredMenus() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class[] filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    Class<IMenu>[] foca = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setFolderMode(getConfiguredFolderMode());
    setShowDirectory(getConfiguredShowDirectory());
    setShowFileName(getConfiguredShowFileName());
    setShowFileExtension(getConfiguredShowFileExtension());
    setTypeLoad(getConfiguredTypeLoad());
    setFileExtensions(getConfiguredFileExtensions());
    if (getConfiguredDirectory() != null) {
      setDirectory(new File(getConfiguredDirectory()));
    }
    setFileIconId(getConfiguredFileIconId());
    setMaxLength(getConfiguredMaxLength());
    // menus
    ArrayList<IMenu> menuList = new ArrayList<IMenu>();
    Class<? extends IMenu>[] a = getConfiguredMenus();
    for (int i = 0; i < a.length; i++) {
      try {
        IMenu menu = ConfigurationUtility.newInnerInstance(this, a[i]);
        menuList.add(menu);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    try {
      injectMenusInternal(menuList);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contributing menus.", e);
    }
    m_menus = menuList.toArray(new IMenu[0]);
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus
   * 
   * @param menuList
   *          live and mutable list of configured menus
   */
  protected void injectMenusInternal(List<IMenu> menuList) {
  }

  @Override
  public IFileChooserFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void setFolderMode(boolean b) {
    m_folderMode = b;
    if (isInitialized()) {
      if (shouldUpdateDisplayText(false)) {
        setDisplayText(execFormatValue(getValue()));
      }
    }
  }

  @Override
  public boolean isFolderMode() {
    return m_folderMode;
  }

  @Override
  public void setShowDirectory(boolean b) {
    m_showDirectory = b;
    if (UserAgentUtility.isWebClient()) {
      m_showDirectory = false;
    }
    if (isInitialized()) {
      if (shouldUpdateDisplayText(false)) {
        setDisplayText(execFormatValue(getValue()));
      }
    }
  }

  @Override
  public boolean isShowDirectory() {
    return m_showDirectory;
  }

  @Override
  public void setShowFileName(boolean b) {
    m_showFileName = b;
    if (isInitialized()) {
      if (shouldUpdateDisplayText(false)) {
        setDisplayText(execFormatValue(getValue()));
      }
    }
  }

  @Override
  public boolean isShowFileName() {
    return m_showFileName;
  }

  @Override
  public void setShowFileExtension(boolean b) {
    m_showFileExtension = b;
    if (isInitialized()) {
      if (shouldUpdateDisplayText(false)) {
        setDisplayText(execFormatValue(getValue()));
      }
    }
  }

  @Override
  public boolean isShowFileExtension() {
    return m_showFileExtension;
  }

  @Override
  public void setTypeLoad(boolean b) {
    m_typeLoad = b;
  }

  @Override
  public boolean isTypeLoad() {
    return m_typeLoad;
  }

  @Override
  public void setFileExtensions(String[] a) {
    m_fileExtensions = a;
  }

  @Override
  public String[] getFileExtensions() {
    return m_fileExtensions;
  }

  @Override
  public void setDirectory(File d) {
    m_directory = d;
  }

  @Override
  public File getDirectory() {
    return m_directory;
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
  public IMenu[] getMenus() {
    return m_menus;
  }

  @Override
  public boolean hasMenus() {
    return m_menus.length > 0;
  }

  @Override
  public IFileChooser getFileChooser() {
    FileChooser fc = new FileChooser();
    fc.setTypeLoad(isTypeLoad());
    fc.setFolderMode(isFolderMode());
    fc.setDirectory(getDirectory());
    fc.setFileName(getFileName());
    fc.setFileExtensions(getFileExtensions());
    fc.setMultiSelect(false);
    return fc;
  }

  // Convenience file getter
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
      File f = new File(s);
      if (isFolderMode()) {
        if (isShowDirectory()) {
          s = f.getAbsolutePath();
        }
        else {
          s = f.getName();
        }
      }
      else {
        if (isShowDirectory() && isShowFileName() && isShowFileExtension()) {
          s = f.getAbsolutePath();
        }
        else {
          String p = StringUtility.valueOf(f.getParent());
          String n = f.getName();
          String e = "";
          if (n.indexOf('.') >= 0) {
            int i = n.lastIndexOf('.');
            e = n.substring(i);
            n = n.substring(0, i);
          }
          s = "";
          if (isShowDirectory()) {
            s = p;
          }
          if (isShowFileName()) {
            if (s.length() > 0) {
              s = s + File.separator;
            }
            s = s + n;
          }
          if (isShowFileExtension()) {
            s = s + e;
          }
        }
      }
    }
    return s;
  }

  @Override
  protected String parseValueInternal(String text) throws ProcessingException {
    String retVal = null;
    if (text != null && text.trim().length() == 0) {
      text = null;
    }
    if (text != null) {
      text = text.trim();
      text = StringUtility.unquoteText(text);
      File f = new File(text);
      String p = "";
      if (f.isAbsolute()) {
        p = f.getParent();
      }
      else {
        // inherit path from existing value
        File existingFile = getValueAsFile();
        if (existingFile != null && existingFile.isAbsolute()) {
          p = existingFile.getParent();
        }
      }
      String n = f.getName();
      String e = "";
      if (n.indexOf('.') >= 0) {
        int i = n.lastIndexOf('.');
        e = n.substring(i);
        n = n.substring(0, i);
      }
      text = n;
      if (p.length() == 0 && getDirectory() != null) {
        p = getDirectory().getAbsolutePath();
      }
      if (e.length() == 0 && m_fileExtensions != null && m_fileExtensions.length > 0) {
        e = "." + m_fileExtensions[0];
      }
      text = p;
      if (p.length() > 0) {
        text += File.separator;
      }
      text += n;
      text += e;
      retVal = text;
    }
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
    public IMenu[] firePopupFromUI() {
      ArrayList<IMenu> menus = new ArrayList<IMenu>();
      for (IMenu menu : getMenus()) {
        menu.prepareAction();
        if (menu.isVisible()) {
          menus.add(menu);
        }
      }
      return menus.toArray(new IMenu[0]);
    }

    @Override
    public boolean setTextFromUI(String newText) {
      if (newText != null && newText.length() == 0) {
        newText = null;
      }
      return parseValue(newText);
    }
  }

}
