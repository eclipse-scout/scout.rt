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
package org.eclipse.scout.rt.client.ui.form.fields.svgfield;

import java.util.EventListener;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.IScoutSVGElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.ScoutSVGModel;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractSvgField extends AbstractValueField<ScoutSVGModel> implements ISvgField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSvgField.class);

  private ISvgFieldUIFacade m_uiFacade;
  private final EventListenerList m_listenerList = new EventListenerList();
  private IMenu[] m_menus;

  public AbstractSvgField() {
  }

  @ConfigPropertyValue("0")
  @Override
  protected int getConfiguredVerticalAlignment() {
    return 0;
  }

  @ConfigPropertyValue("0")
  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 0;
  }

  private Class<? extends IMenu>[] getConfiguredMenus() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IMenu.class);
  }

  /**
   * called when a svg item was clicked
   */
  @ConfigOperation
  @Order(10)
  protected void execElementClicked(SvgFieldEvent event) throws ProcessingException {
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    // menus
    Class<? extends IMenu>[] a = getConfiguredMenus();
    m_menus = new IMenu[a != null ? a.length : 0];
    if (a != null) {
      for (int i = 0; i < m_menus.length; i++) {
        try {
          m_menus[i] = ConfigurationUtility.newInnerInstance(this, a[i]);
        }
        catch (Exception e) {
          LOG.warn(null, e);
        }
      }
    }
  }

  @Override
  public IScoutSVGElement getSelectedElement() {
    return (IScoutSVGElement) propertySupport.getProperty(PROP_SELECTED_ELEMENT);
  }

  @Override
  public void setSelectedElement(IScoutSVGElement element) {
    propertySupport.setProperty(PROP_SELECTED_ELEMENT, resolveElement(element));
  }

  @Override
  public void addSvgFieldListener(ISvgFieldListener listener) {
    m_listenerList.add(ISvgFieldListener.class, listener);
  }

  @Override
  public void removeSvgFieldListener(ISvgFieldListener listener) {
    m_listenerList.remove(ISvgFieldListener.class, listener);
  }

  @Override
  public IMenu[] getMenus() {
    return m_menus;
  }

  /*
   * UI accessible
   */
  @Override
  public ISvgFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  protected void valueChangedInternal() {
    //restore selection by id
    IScoutSVGElement oldSelectedElement = getSelectedElement();
    if (oldSelectedElement != null) {
      ScoutSVGModel model = getValue();
      IScoutSVGElement newSelectedElement = model != null ? model.findGraphicalElement(oldSelectedElement.getId()) : null;
      if (oldSelectedElement != newSelectedElement) {
        setSelectedElement(newSelectedElement);
      }
    }
    super.valueChangedInternal();
  }

  private void fireClick() {
    IScoutSVGElement selectedElement = getSelectedElement();
    if (selectedElement == null) {
      return;
    }
    SvgFieldEvent e = new SvgFieldEvent(this, SvgFieldEvent.TYPE_ELEMENT_CLICKED, selectedElement);
    // single observer
    try {
      execElementClicked(e);
    }
    catch (ProcessingException pe) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(pe);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
    }
    if (!e.isConsumed()) {
      fireSvgFieldEventInternal(e);
    }
  }

  private IMenu[] firePopup() {
    IScoutSVGElement selectedElement = getSelectedElement();
    SvgFieldEvent e = new SvgFieldEvent(this, SvgFieldEvent.TYPE_ELEMENT_POPUP, selectedElement);
    // single observer
    IMenu[] a = getMenus();
    for (int i = 0; i < a.length; i++) {
      IMenu m = a[i];
      m.prepareAction();
      if (m.isVisible()) {
        e.addPopupMenu(m);
      }
    }
    fireSvgFieldEventInternal(e);
    return e.getPopupMenus();
  }

  private void fireSvgFieldEventInternal(SvgFieldEvent e) {
    EventListener[] a = m_listenerList.getListeners(ISvgFieldListener.class);
    if (a != null) {
      for (int i = 0; i < a.length; i++) {
        ((ISvgFieldListener) a[i]).svgFieldChanged(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  private IScoutSVGElement resolveElement(IScoutSVGElement elem) {
    ScoutSVGModel model = getValue();
    if (model == null) {
      return null;
    }
    for (IScoutSVGElement owned : model.getGraphicsElements()) {
      if (owned == elem) {
        return owned;
      }
    }
    return null;
  }

  private class P_UIFacade implements ISvgFieldUIFacade {

    @Override
    public void setSelectedElementFromUI(IScoutSVGElement element) {
      setSelectedElement(resolveElement(element));
    }

    @Override
    public void fireElementClickFromUI(IScoutSVGElement element) {
      setSelectedElement(resolveElement(element));
      fireClick();
    }

    @Override
    public IMenu[] fireElementPopupFromUI(IScoutSVGElement element) {
      setSelectedElement(resolveElement(element));
      return firePopup();
    }
  }

}
