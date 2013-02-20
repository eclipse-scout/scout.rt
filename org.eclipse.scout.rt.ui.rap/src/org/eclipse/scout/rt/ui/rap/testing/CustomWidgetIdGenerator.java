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
package org.eclipse.scout.rt.ui.rap.testing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.widgets.Widget;

/**
 * This generator creates ids based on the scout model instead of just generating a sequence number as the default
 * generator does.
 * <p>
 * In the following situations it's not guaranteed that the widget always gets the same id: <br>
 * - If the same form is opened multiple times<br>
 * - If the scout object is not a form field and used inside a template field. For example a {@link IMenu} used in a
 * smartfield template.
 * 
 * @since 3.8.2
 */
public class CustomWidgetIdGenerator {
  private Map<String, Integer> m_existingIds = new HashMap<String, Integer>();

  public static CustomWidgetIdGenerator getInstance() {
    return SingletonUtil.getSessionInstance(CustomWidgetIdGenerator.class);
  }

  private CustomWidgetIdGenerator() {
  }

  /**
   * Computes the widget id with the use of {@link #newId(Widget, IPropertyObserver, String)} and sets it on the widget
   * and its children, if they don't already have a custom id. The ids
   * for these widget are the same beside the suffix which is a sequence number.
   */
  public void setCustomWidgetIds(Widget widget, IPropertyObserver scoutObject, String prefix) {
    int childIndex = 0;

    List<Widget> children = RwtUtility.findChildComponents(widget, Widget.class);
    String customWidgetId = newId(widget, scoutObject, prefix);
    for (Widget childWidget : children) {
      if (childWidget.getData(WidgetUtil.CUSTOM_WIDGET_ID) == null) {
        childWidget.setData(WidgetUtil.CUSTOM_WIDGET_ID, newChildId(customWidgetId, childIndex));
        childIndex++;
      }
    }
  }

  private String newChildId(String parentId, int childIndex) {
    return parentId + "_c" + childIndex;
  }

  /**
   * Creates a unique id based on the scout object and the widget.
   * <p>
   * If the scout object is a {@link IFormField}, the id consists of the given prefix, the package name of the form,
   * {@link IFormField#getFieldId()}, {@link IForm#getFormId()} and the {@link IFormField#getFieldId()} of the parents,
   * if necessary. <br>
   * In the other cases, the id consists of the prefix and class name including the package.
   */
  public String newId(Widget widget, IPropertyObserver scoutObject, String prefix) {
    String scoutObjectId = createScoutObjectId(scoutObject, widget);
    String id = prefix + "_" + scoutObjectId;
    //Replace invalid characters, see also UITestUtil.isValidId
    id = id.replace("$", ".");

    //The ids have to be unique, therefore a sequence number is attached
    Integer useCount = m_existingIds.get(id);
    if (useCount != null) {
      useCount++;
    }
    else {
      useCount = 1;
    }
    m_existingIds.put(id, useCount);
    id = id + "_" + (useCount - 1);

    return id;
  }

  private String createScoutObjectId(IPropertyObserver scoutObject, Widget widget) {
    String scoutObjectId = "";
    if (scoutObject instanceof IFormField) {
      scoutObjectId = createFormFieldId((IFormField) scoutObject);
    }
    else {
      scoutObjectId = scoutObject.getClass().getName();
    }

    return scoutObjectId;
  }

  private String createFormFieldId(IFormField formField) {
    IFormField topField;
    String scoutObjectId = "";
    List<ICompositeField> enclosingFieldList = formField.getEnclosingFieldList();
    if (enclosingFieldList != null && enclosingFieldList.size() > 0) {
      for (ICompositeField parentField : enclosingFieldList) {
        scoutObjectId = parentField.getFieldId() + "." + scoutObjectId;
      }
      topField = enclosingFieldList.get(enclosingFieldList.size() - 1);
    }
    else {
      topField = formField;
      scoutObjectId = formField.getFieldId();
    }
    if (topField.getForm() != null) {
      scoutObjectId = topField.getForm().getFormId() + "." + scoutObjectId;
    }
    scoutObjectId = topField.getClass().getPackage().getName() + "." + scoutObjectId;

    return scoutObjectId;
  }
}
