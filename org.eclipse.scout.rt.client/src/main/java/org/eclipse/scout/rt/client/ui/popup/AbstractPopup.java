/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.popup;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * @since 9.0
 */
@ClassId("8d02c4fd-2d8b-4630-9fd2-69fea7f8c364")
public abstract class AbstractPopup extends AbstractWidget implements IPopup {

  private IPopupUIFacade m_uiFacade;

  public AbstractPopup() {
    this(true);
  }

  public AbstractPopup(boolean callInitializer) {
    super(callInitializer);
  }

  protected IWidget getConfiguredAnchor() {
    return null;
  }

  @Override
  public IWidget getAnchor() {
    return (IWidget) propertySupport.getProperty(PROP_ANCHOR);
  }

  @Override
  public void setAnchor(IWidget anchor) {
    propertySupport.setProperty(PROP_ANCHOR, anchor);
  }

  protected boolean getConfiguredAnimateOpening() {
    return false;
  }

  @Override
  public boolean isAnimateOpening() {
    return propertySupport.getPropertyBool(PROP_ANIMATE_OPENING);
  }

  @Override
  public void setAnimateOpening(boolean animateOpening) {
    propertySupport.setPropertyBool(PROP_ANIMATE_OPENING, animateOpening);
  }

  protected boolean getConfiguredAnimateResize() {
    return false;
  }

  @Override
  public boolean isAnimateResize() {
    return propertySupport.getPropertyBool(PROP_ANIMATE_RESIZE);
  }

  @Override
  public void setAnimateResize(boolean animateResize) {
    propertySupport.setPropertyBool(PROP_ANIMATE_RESIZE, animateResize);
  }

  protected boolean getConfiguredWithGlassPane() {
    return false;
  }

  @Override
  public boolean isWithGlassPane() {
    return propertySupport.getPropertyBool(PROP_WITH_GLASS_PANE);
  }

  @Override
  public void setWithGlassPane(boolean withGlassPane) {
    propertySupport.setPropertyBool(PROP_WITH_GLASS_PANE, withGlassPane);
  }

  protected String getConfiguredScrollType() {
    return SCROLL_TYPE_REMOVE;
  }

  @Override
  public String getScrollType() {
    return propertySupport.getPropertyString(PROP_SCROLL_TYPE);
  }

  @Override
  public void setScrollType(String scrollType) {
    propertySupport.setPropertyString(PROP_SCROLL_TYPE, scrollType);
  }

  protected boolean getConfiguredTrimWidth() {
    return false;
  }

  @Override
  public boolean isTrimWidth() {
    return propertySupport.getPropertyBool(PROP_TRIM_WIDTH);
  }

  @Override
  public void setTrimWidth(boolean trimWidth) {
    propertySupport.setPropertyBool(PROP_TRIM_WIDTH, trimWidth);
  }

  protected boolean getConfiguredTrimHeight() {
    return true;
  }

  @Override
  public boolean isTrimHeight() {
    return propertySupport.getPropertyBool(PROP_TRIM_HEIGHT);
  }

  @Override
  public void setTrimHeight(boolean trimHeight) {
    propertySupport.setPropertyBool(PROP_TRIM_HEIGHT, trimHeight);
  }

  protected String getConfiguredHorizontalAlignment() {
    return POPUP_ALIGNMENT_LEFTEDGE;
  }

  @Override
  public String getHorizontalAlignment() {
    return propertySupport.getPropertyString(PROP_HORIZONTAL_ALIGNMENT);
  }

  @Override
  public void setHorizontalAlignment(String horizontalAlignment) {
    propertySupport.setPropertyString(PROP_HORIZONTAL_ALIGNMENT, horizontalAlignment);
  }

  protected String getConfiguredVerticalAlignment() {
    return POPUP_ALIGNMENT_BOTTOM;
  }

  @Override
  public String getVerticalAlignment() {
    return propertySupport.getPropertyString(PROP_VERTICAL_ALIGNMENT);
  }

  @Override
  public void setVerticalAlignment(String verticalAlignment) {
    propertySupport.setPropertyString(PROP_VERTICAL_ALIGNMENT, verticalAlignment);
  }

  protected boolean getConfiguredWithArrow() {
    return false;
  }

  @Override
  public boolean isWithArrow() {
    return propertySupport.getPropertyBool(PROP_WITH_ARROW);
  }

  @Override
  public void setWithArrow(boolean withArrow) {
    propertySupport.setPropertyBool(PROP_WITH_ARROW, withArrow);
  }

  protected boolean getConfiguredCloseOnAnchorMouseDown() {
    return true;
  }

  @Override
  public boolean isCloseOnAnchorMouseDown() {
    return propertySupport.getPropertyBool(PROP_CLOSE_ON_ANCHOR_MOUSE_DOWN);
  }

  @Override
  public void setCloseOnAnchorMouseDown(boolean closeOnAnchorMouseDown) {
    propertySupport.setPropertyBool(PROP_CLOSE_ON_ANCHOR_MOUSE_DOWN, closeOnAnchorMouseDown);
  }

  protected boolean getConfiguredCloseOnMouseDownOutside() {
    return true;
  }

  @Override
  public boolean isCloseOnMouseDownOutside() {
    return propertySupport.getPropertyBool(PROP_CLOSE_ON_MOUSE_DOWN_OUTSIDE);
  }

  @Override
  public void setCloseOnMouseDownOutside(boolean closeOnMouseDownOutside) {
    propertySupport.setPropertyBool(PROP_CLOSE_ON_MOUSE_DOWN_OUTSIDE, closeOnMouseDownOutside);
  }

  protected boolean getConfiguredCloseOnOtherPopupOpen() {
    return true;
  }

  @Override
  public boolean isCloseOnOtherPopupOpen() {
    return propertySupport.getPropertyBool(PROP_CLOSE_ON_OTHER_POPUP_OPEN);
  }

  @Override
  public void setCloseOnOtherPopupOpen(boolean closeOnOtherPopupOpen) {
    propertySupport.setPropertyBool(PROP_CLOSE_ON_OTHER_POPUP_OPEN, closeOnOtherPopupOpen);
  }

  protected boolean getConfiguredHorizontalSwitch() {
    return false;
  }

  @Override
  public boolean isHorizontalSwitch() {
    return propertySupport.getPropertyBool(PROP_HORIZONTAL_SWITCH);
  }

  @Override
  public void setHorizontalSwitch(boolean horizontalSwitch) {
    propertySupport.setPropertyBool(PROP_HORIZONTAL_SWITCH, horizontalSwitch);
  }

  protected boolean getConfiguredVerticalSwitch() {
    return true;
  }

  @Override
  public boolean isVerticalSwitch() {
    return propertySupport.getPropertyBool(PROP_VERTICAL_SWITCH);
  }

  @Override
  public void setVerticalSwitch(boolean verticalSwitch) {
    propertySupport.setPropertyBool(PROP_VERTICAL_SWITCH, verticalSwitch);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());

    setAnchor(getConfiguredAnchor());
    setAnimateOpening(getConfiguredAnimateOpening());
    setAnimateResize(getConfiguredAnimateResize());
    setWithGlassPane(getConfiguredWithGlassPane());
    setScrollType(getConfiguredScrollType());
    setTrimWidth(getConfiguredTrimWidth());
    setTrimHeight(getConfiguredTrimHeight());
    setHorizontalAlignment(getConfiguredHorizontalAlignment());
    setVerticalAlignment(getConfiguredVerticalAlignment());
    setWithArrow(getConfiguredWithArrow());
    setCloseOnAnchorMouseDown(getConfiguredCloseOnAnchorMouseDown());
    setCloseOnMouseDownOutside(getConfiguredCloseOnMouseDownOutside());
    setCloseOnOtherPopupOpen(getConfiguredCloseOnOtherPopupOpen());
    setHorizontalSwitch(getConfiguredHorizontalSwitch());
    setVerticalSwitch(getConfiguredVerticalSwitch());
  }

  @Override
  public IPopupUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements IPopupUIFacade {
    @Override
    public void firePopupClosingFromUI() {
      close();
    }
  }

  protected PopupManager getPopupManager() {
    return IDesktop.CURRENT.get().getAddOn(PopupManager.class);
  }

  @Override
  public void open() {
    getPopupManager().open(this);
  }

  @Override
  public void close() {
    getPopupManager().close(this);
    dispose();
  }
}
