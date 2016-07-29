/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields.tabbox;

import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.groupbox.JsonGroupBox;

/**
 * There is no Scout model-class for a TabItem. We simply use the GroupBox class in that case. Problem with that
 * approach is, that the GroupBox does not have a "marked" property. That's why the JSON Layer must store that state. In
 * a future Scout release we should create a new AbstractTabItem class extending GroupBox, adding the missing property.
 * Than we could move a lot of code from here to the new model class.
 *
 * @param <GROUP_BOX>
 */
public class JsonTabItem<GROUP_BOX extends IGroupBox> extends JsonGroupBox<GROUP_BOX> {

  public static final String PROP_MARKED = "marked";

  private boolean m_marked;

  public JsonTabItem(GROUP_BOX model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TabItem";
  }

  @Override
  protected void initJsonProperties(GROUP_BOX model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IGroupBox>(PROP_MARKED, model) {
      @Override
      protected Boolean modelValue() {
        return m_marked;
      }
    });
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    handleModelSaveNeededChanged();
    handleModelEmptyChanged();
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (IGroupBox.PROP_EMPTY.equals(propertyName)) {
      handleModelEmptyChanged();
    }
    else if (IGroupBox.PROP_SAVE_NEEDED.equals(propertyName)) {
      handleModelSaveNeededChanged();
    }
    else {
      super.handleModelPropertyChange(propertyName, oldValue, newValue);
    }
  }

  protected boolean updateMarker(int markStrategy) {
    ICompositeField parent = getModel().getParentField();
    if (parent instanceof ITabBox) {
      return ((ITabBox) parent).getMarkStrategy() == markStrategy;
    }
    return false;
  }

  protected void handleModelSaveNeededChanged() {
    if (updateMarker(ITabBox.MARK_STRATEGY_SAVE_NEEDED)) {
      m_marked = getModel().isSaveNeeded();
      addMarkedPropertyChangeEvent();
    }
  }

  protected void handleModelEmptyChanged() {
    if (updateMarker(ITabBox.MARK_STRATEGY_EMPTY)) {
      m_marked = !getModel().isEmpty();
      addMarkedPropertyChangeEvent();
    }
  }

  protected void addMarkedPropertyChangeEvent() {
    addPropertyChangeEvent(PROP_MARKED, m_marked);
  }
}
