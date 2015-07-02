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
package org.eclipse.scout.rt.ui.html.json.form.fields.tabbox;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.groupbox.JsonGroupBox;

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

  /* TODO AWE: (scout) das problem ist, dass es kein model für TabItem gibt. Man nimmt einfach
   * die GroupBox als Model. Dieses hat aber keine "marked" Property. Darum muss sich das UI
   * künstlich den marked Zustand merken (siehe Swing-Code). In der neuen Scout-Version sollte
   * es ein AbstractTabItem geben, könnte GroupBox extenden. Dann könnte viel Code von hier auf
   * dem Model behandelt werden.
   */

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

  private boolean updateMarker(int markStrategy) {
    ICompositeField parent = getModel().getParentField();
    if (parent instanceof ITabBox) {
      return ((ITabBox) parent).getMarkStrategy() == markStrategy;
    }
    // TODO AWE: (scout) check if this legacy-else-if is still required
    else if (getModel().getForm() instanceof ISearchForm) {
      // legacy
      return true;
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

  private void addMarkedPropertyChangeEvent() {
    addPropertyChangeEvent(PROP_MARKED, m_marked);
  }

}
