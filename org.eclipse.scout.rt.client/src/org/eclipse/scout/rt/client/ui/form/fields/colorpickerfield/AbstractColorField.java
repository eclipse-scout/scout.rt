/*******************************************************************************
 * Copyright (c) 2014 Schweizerische Bundesbahnen SBB, BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Schweizerische Bundesbahnen SBB - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.colorpickerfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ColorUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractColorField extends AbstractBasicField<String> implements IColorField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractColorField.class);

  private IColorFieldUiFacade m_uiFacade;
  private List<IMenu> m_menus;

  protected static final Pattern RGB_COLOR_PATTERN = Pattern.compile("^([0-9]{1,3})[\\-\\,\\;\\/\\\\\\s]{1}([0-9]{1,3})[\\-\\,\\;\\/\\\\\\s]{1}([0-9]{1,3})$");

  public AbstractColorField() {
    this(true);
  }

  public AbstractColorField(boolean callInitializer) {
    super(callInitializer);
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(230)
  protected String getConfiguredIconId() {
    return AbstractIcons.Palette;
  }

  private List<Class<? extends IMenu>> getConfiguredMenus() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<?>> dcl = Arrays.asList(dca);
    List<Class<? extends IMenu>> foca = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dcl, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = new P_UiFacade();
    setIconId(getConfiguredIconId());

    // menus
    List<IMenu> menuList = new ArrayList<IMenu>();
    List<Class<? extends IMenu>> menus = getConfiguredMenus();
    for (int i = 0; i < menus.size(); i++) {
      IMenu menu;
      try {
        menu = ConfigurationUtility.newInnerInstance(this, menus.get(i));
        menuList.add(menu);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("menu: " + menus.get(i).getName(), t));
      }
    }
    try {
      injectMenusInternal(menuList);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contributing menus.", e);
    }
    m_menus = menuList;
  }

  @Override
  public IColorFieldUiFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  protected String parseValueInternal(String text) throws ProcessingException {
    // hex
    if (StringUtility.isNullOrEmpty(text)) {
      return null;
    }
    try {
      // try to parse hex
      Matcher matcher = ColorUtility.HEX_COLOR_PATTERN.matcher(text);
      if (matcher.matches()) {
        return "#" + matcher.group(2);
      }
      // try to parse any kind of RGB

      matcher = RGB_COLOR_PATTERN.matcher(text);
      if (matcher.matches()) {
        int r = Integer.valueOf(matcher.group(1));
        int g = Integer.valueOf(matcher.group(2));
        int b = Integer.valueOf(matcher.group(3));
        if (r < 0 || r > 255 ||
            g < 0 || g > 255 ||
            b < 0 || b > 255) {
          throw new ProcessingException(ScoutTexts.get("InvalidValueMessageX", text));
        }
        String hexValue = ColorUtility.rgbToText(r, g, b).toUpperCase();
        return hexValue;
      }
    }
    catch (Exception e) {
      throw new ProcessingException(ScoutTexts.get("InvalidValueMessageX", text), e);
    }
    throw new ProcessingException(ScoutTexts.get("InvalidValueMessageX", text));
  }

  @Override
  public String getIconId() {
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  @Override
  public void setIconId(String s) {
    propertySupport.setPropertyString(PROP_ICON_ID, s);
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
  public List<IMenu> getMenus() {
    return CollectionUtility.unmodifiableListCopy(m_menus);
  }

  protected List<IMenu> firePopup() {
    return MenuUtility.filterValidMenus(this, getMenus(), true);
  }

  // ---------------------------------------------------------------------------------
  private class P_UiFacade implements IColorFieldUiFacade {
    @Override
    public boolean setTextFromUI(String newText, boolean whileTyping) {
      if (newText != null && newText.length() == 0) {
        newText = null;
      }
      // parse always, validity might change even if text is same
      setWhileTyping(whileTyping);
      return parseValue(newText);
    }

    @Override
    public void setValueFromUi(String value) {
      if (StringUtility.hasText(value)) {
        setValue(value);
      }
      else {
        setValue(null);
      }
    }

    @Override
    public List<IMenu> firePopupFromUI() {
      return firePopup();
    }
  }

}
