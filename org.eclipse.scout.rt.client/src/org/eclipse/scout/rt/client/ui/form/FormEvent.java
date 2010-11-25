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
package org.eclipse.scout.rt.client.ui.form;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.pagefield.IPageField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;

/**
 * Form lifecycle for observing form "open" event attach to IDesktop and listen
 * for FORM_ADDED
 */
public class FormEvent extends EventObject {
  private static final long serialVersionUID = 1L;
  // state
  public static final int TYPE_ACTIVATED = 510;
  public static final int TYPE_LOAD_BEFORE = 1000;
  public static final int TYPE_LOAD_AFTER = 1010;
  public static final int TYPE_LOAD_COMPLETE = 1020;
  public static final int TYPE_STORE_BEFORE = 2010;
  public static final int TYPE_STORE_AFTER = 2020;
  public static final int TYPE_DISCARDED = 3000;
  public static final int TYPE_CLOSED = 3010;
  /**
   * print a form using properties formField, printDevice, printParameters
   */
  public static final int TYPE_PRINT = 4000;
  /**
   * When the field structure changes Examples: a field changes its "visible"
   * property a {@link IWrappedFormField} changes its inner form a {@link IPageField} changes its table/search/detail a
   * custom field changes
   * in a way that the form structure is different
   */
  public static final int TYPE_STRUCTURE_CHANGED = 5000;
  public static final int TYPE_TO_FRONT = 6000;
  public static final int TYPE_TO_BACK = 6010;

  private final int m_type;
  private IFormField m_formField;
  private PrintDevice m_printDevice;
  private Map<String, Object> m_printParameters;

  /**
   * A form event is sent whenever a form changes. You can register
   * to receive such events via {@link IForm#addFormListener(FormListener)}.
   * The form listener will get sent form events via <code>formChanged</code>.
   * Once you implement your form listener and receive your form events, you'll
   * probably be interested in the <b>type</b> you get via <code>getType</code>.
   */
  public FormEvent(IForm form, int type) {
    super(form);
    m_type = type;
  }

  public FormEvent(IForm form, int type, IFormField causingField) {
    super(form);
    m_type = type;
    m_formField = causingField;
  }

  public FormEvent(IForm form, int type, IFormField printRoot, PrintDevice printDevice, Map<String, Object> printParameters) {
    super(form);
    m_type = type;
    m_formField = printRoot;
    m_printDevice = printDevice;
    m_printParameters = printParameters;
  }

  public IForm getForm() {
    return (IForm) getSource();
  }

  public int getType() {
    return m_type;
  }

  public IFormField getFormField() {
    return m_formField;
  }

  public PrintDevice getPrintDevice() {
    return m_printDevice;
  }

  public Map<String, Object> getPrintParameters() {
    if (m_printParameters != null) {
      return new HashMap<String, Object>(m_printParameters);
    }
    else {
      return new HashMap<String, Object>();
    }
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("DialogEvent[");
    // decode type
    try {
      Field[] f = getClass().getDeclaredFields();
      for (int i = 0; i < f.length; i++) {
        if (Modifier.isPublic(f[i].getModifiers()) && Modifier.isStatic(f[i].getModifiers()) && f[i].getName().startsWith("TYPE_")) {
          if (((Number) f[i].get(null)).intValue() == m_type) {
            buf.append(f[i].getName());
            break;
          }
        }
      }
    }
    catch (Throwable t) {
      buf.append("#" + m_type);
    }
    // dialog
    if (getForm() != null) {
      buf.append(" " + getForm().getFormId());
    }
    buf.append("]");
    return buf.toString();
  }

}
