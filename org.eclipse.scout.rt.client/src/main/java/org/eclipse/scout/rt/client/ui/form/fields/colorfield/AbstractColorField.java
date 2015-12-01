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
package org.eclipse.scout.rt.client.ui.form.fields.colorfield;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.colorfield.IColorFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicFieldUIFacade;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.ColorUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;

public abstract class AbstractColorField extends AbstractBasicField<String> implements IColorField {
  private IBasicFieldUIFacade m_uiFacade;

  protected static final Pattern RGB_COLOR_PATTERN = Pattern.compile("^([0-9]{1,3})[\\-\\,\\;\\/\\\\\\s]{1}([0-9]{1,3})[\\-\\,\\;\\/\\\\\\s]{1}([0-9]{1,3})$");

  public AbstractColorField() {
    this(true);
  }

  public AbstractColorField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
  }

  @Override
  public IBasicFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  protected String parseValueInternal(String text) {
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
        if (r < 0 || r > 255
            || g < 0 || g > 255
            || b < 0 || b > 255) {
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

  protected static class LocalColorFieldExtension<OWNER extends AbstractColorField> extends LocalBasicFieldExtension<String, OWNER> implements IColorFieldExtension<OWNER> {

    public LocalColorFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IColorFieldExtension<? extends AbstractColorField> createLocalExtension() {
    return new LocalColorFieldExtension<AbstractColorField>(this);
  }

}
