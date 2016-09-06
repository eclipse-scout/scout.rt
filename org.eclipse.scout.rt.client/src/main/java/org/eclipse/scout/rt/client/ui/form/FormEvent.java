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
package org.eclipse.scout.rt.client.ui.form;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.pagefield.IPageField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;

/**
 * Form lifecycle for observing form "open" event attach to IDesktop and listen for FORM_ADDED
 */
public class FormEvent extends EventObject implements IModelEvent {
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
   * When the field structure changes Examples: a field changes its "visible" property a {@link IWrappedFormField}
   * changes its inner form a {@link IPageField} changes its table/search/detail a custom field changes in a way that
   * the form structure is different
   */
  public static final int TYPE_STRUCTURE_CHANGED = 5000;
  /**
   * see {@link IForm#toFront()}
   */
  public static final int TYPE_TO_FRONT = 6000;
  /**
   * see {@link IForm#toBack()}
   */
  public static final int TYPE_TO_BACK = 6010;
  /**
   * see {@link IFormField#requestFocus()}
   */
  public static final int TYPE_REQUEST_FOCUS = 6020;
  //next 6030

  private final int m_type;
  private final IFormField m_formField;

  /**
   * A form event is sent whenever a form changes. You can register to receive such events via
   * {@link IForm#addFormListener(FormListener)}. The form listener will get sent form events via
   * <code>formChanged</code>. Once you implement your form listener and receive your form events, you'll probably be
   * interested in the <b>type</b> you get via <code>getType</code>.
   */
  public FormEvent(IForm form, int type) {
    this(form, type, null);
  }

  public FormEvent(IForm form, int type, IFormField causingField) {
    super(form);
    m_type = type;
    m_formField = causingField;
  }

  public IForm getForm() {
    return (IForm) getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }

  public IFormField getFormField() {
    return m_formField;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("DialogEvent[");
    // decode type
    try {
      Field[] f = getClass().getDeclaredFields();
      for (int i = 0; i < f.length; i++) {
        if (Modifier.isPublic(f[i].getModifiers())
            && Modifier.isStatic(f[i].getModifiers())
            && f[i].getName().startsWith("TYPE_")
            && ((Number) f[i].get(null)).intValue() == m_type) {
          buf.append(f[i].getName());
          break;
        }
      }
    }
    catch (Exception t) { // NOSONAR
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
