/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.IOutlineViewButtonExtension;
import org.eclipse.scout.rt.client.ui.action.view.AbstractViewButton;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

@ClassId("401907e2-6767-435b-8452-9c819f3af82f")
public abstract class AbstractOutlineViewButton extends AbstractViewButton implements IOutlineViewButton {

  private final IDesktop m_desktop;
  private final IOutline m_outline;

  /**
   * call using {@link AbstractDesktop}.this or {@link AbstractDesktopExtension#getDelegatingDesktop()}
   */
  public AbstractOutlineViewButton(IDesktop desktop, Class<? extends IOutline> outlineType) {
    this(desktop, getOutlineOfType(desktop, outlineType));
  }

  protected static IOutline getOutlineOfType(IDesktop desktop, Class<? extends IOutline> outlineType) {
    if (desktop == null) {
      return null;
    }

    for (IOutline o : desktop.getAvailableOutlines()) {
      if (o.getClass() == outlineType) {
        return o;
      }
    }

    throw new AssertionException("the outline type {} is not registered in the desktop.", outlineType);
  }

  public AbstractOutlineViewButton(IDesktop desktop, IOutline outline) {
    super(false);
    m_desktop = Assertions.assertNotNull(desktop, "Desktop must not be null");
    m_outline = Assertions.assertNotNull(outline, "Outline must not be null");
    callInitializer();
  }

  @Override
  protected void execInitAction() {
    setVisibleGranted(m_outline.isVisibleGranted());
    setVisible(m_outline.isVisible());
    setEnabled(m_outline.isEnabled());
    setText(m_outline.getTitle());
    setIconId(m_outline.getIconId());
    setSelected(m_desktop.getOutline() == m_outline);
    // add selection listener
    m_desktop.addDesktopListener(
        e -> {
          switch (e.getType()) {
            case DesktopEvent.TYPE_OUTLINE_CHANGED: {
              setSelected(e.getOutline() == m_outline);
              break;
            }
          }
        });
    // add change listener
    m_outline.addPropertyChangeListener(
        e -> {
          String n = e.getPropertyName();
          Object v = e.getNewValue();
          if (n.equals(IOutline.PROP_VISIBLE)) {
            setVisible((Boolean) v);
          }
          else if (n.equals(IOutline.PROP_TITLE)) {
            setText((String) v);
          }
          else if (n.equals(IOutline.PROP_ICON_ID)) {
            setIconId((String) v);
          }
        });
  }

  /**
   * Activates the associated outline if it is not the desktop's active outline.
   */
  @Override
  protected void execAction() {
    if (isSelected() && m_desktop.getOutline() != m_outline) {
      m_desktop.activateOutline(m_outline);
    }
  }

  /**
   * since execInitAction sets the value of this method to the value of the outline. The getConfigured does not have any
   * affect.
   */
  @Override
  @SuppressWarnings("squid:S1185") // method is final
  protected final boolean getConfiguredEnabled() {
    return super.getConfiguredEnabled();
  }

  /**
   * since execInitAction sets the value of this method to the value of the outline. The getConfigured does not have any
   * affect.
   */
  @Override
  @SuppressWarnings("squid:S1185") // method is final
  protected final String getConfiguredText() {
    return super.getConfiguredText();
  }

  /**
   * since execInitAction sets the value of this method to the value of the outline. The getConfigured does not have any
   * affect.
   */
  @Override
  @SuppressWarnings("squid:S1185") // method is final
  protected final boolean getConfiguredVisible() {
    return super.getConfiguredVisible();
  }

  @Override
  public IOutline getOutline() {
    return m_outline;
  }

  protected static class LocalOutlineViewButtonExtension<OWNER extends AbstractOutlineViewButton> extends LocalViewButtonExtension<OWNER> implements IOutlineViewButtonExtension<OWNER> {

    public LocalOutlineViewButtonExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IOutlineViewButtonExtension<? extends AbstractOutlineViewButton> createLocalExtension() {
    return new LocalOutlineViewButtonExtension<>(this);
  }

}
