/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.contenteditor;

import java.util.EventListener;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.EventListenerList;

@ClassId("182a9023-67f0-4e15-b7cd-3453fc64a8dd")
public abstract class AbstractContentEditorField extends AbstractFormField implements IContentEditorField {

  private IContentEditorFieldUIFacade m_uiFacade;
  private final EventListenerList m_listenerList = new EventListenerList();

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();
  }

  @Override
  public IContentEditorFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void addContentEditorFieldListener(ContentEditorFieldListener listener) {
    m_listenerList.add(ContentEditorFieldListener.class, listener);
  }

  @Override
  public void removeContentEditorFieldListener(ContentEditorFieldListener listener) {
    m_listenerList.remove(ContentEditorFieldListener.class, listener);
  }

  protected void fireContentEditorFieldEvent(ContentEditorFieldEvent e) {
    EventListener[] listeners = m_listenerList.getListeners(ContentEditorFieldListener.class);
    if (listeners == null) {
      return;
    }
    for (EventListener listener : listeners) {
      ((ContentEditorFieldListener) listener).contentEditorFieldChanged(e);
    }
  }

  protected void fireContentElementUpdate(ContentElement contentElement) {
    ContentEditorFieldEvent event = new ContentEditorFieldEvent(this, ContentEditorFieldEvent.TYPE_CONTENT_ELEMENT_UPDATE);
    event.setContentElement(contentElement);
    fireContentEditorFieldEvent(event);
  }

  @Override
  public void setContent(String content) {
    propertySupport.setPropertyString(PROP_CONTENT, content);
  }

  @Override
  public String getContent() {
    return propertySupport.getPropertyString(PROP_CONTENT);
  }

  // FIXME cbu: add intercept method and chain classes
  @Override
  public void execEditElement(ContentElement contentElement) {
  }

  @Override
  public void updateElement(ContentElement contentElement) {
    fireContentElementUpdate(contentElement);
  }

  protected class P_UIFacade implements IContentEditorFieldUIFacade {

    @Override
    public void editElementFromUi(ContentElement contentElement) {
      execEditElement(contentElement);
    }
  }
}
