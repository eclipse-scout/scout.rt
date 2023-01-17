/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.colorfield;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.colorfield.IColorFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicFieldUIFacade;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.ColorUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

@ClassId("1411a921-017a-4d64-b898-9ab01b9fa73a")
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
        int r = Integer.parseInt(matcher.group(1));
        int g = Integer.parseInt(matcher.group(2));
        int b = Integer.parseInt(matcher.group(3));
        if (r < 0 || r > 255
            || g < 0 || g > 255
            || b < 0 || b > 255) {
          throw new ProcessingException(TEXTS.get("InvalidValueMessageX", text));
        }
        String hexValue = ColorUtility.rgbToText(r, g, b).toUpperCase();
        return hexValue;
      }
    }
    catch (Exception e) {
      throw new ProcessingException(TEXTS.get("InvalidValueMessageX", text), e);
    }
    throw new ProcessingException(TEXTS.get("InvalidValueMessageX", text));
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
    return new LocalColorFieldExtension<>(this);
  }

}
