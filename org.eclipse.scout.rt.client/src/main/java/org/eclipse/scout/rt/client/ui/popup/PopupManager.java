/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.popup;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

import java.util.Set;

/**
 * @since 9.0
 */
@Bean
public class PopupManager extends AbstractPropertyObserver {

  public static final String PROP_POPUPS = "popups";

  public PopupManager() {
    propertySupport.setPropertySet(PROP_POPUPS, CollectionUtility.emptyHashSet());
  }

  public Set<IPopup> getPopups() {
    return CollectionUtility.hashSet(getPopupsInternal());
  }

  protected Set<IPopup> getPopupsInternal() {
    return propertySupport.getPropertySet(PROP_POPUPS);
  }

  @SuppressWarnings("unchecked")
  public <T extends IPopup> T getPopupByClass(Class<T> popupClass) {
    return (T) getPopupsInternal().stream().filter(popupClass::isInstance).findFirst().orElse(null);
  }

  protected void setPopupsInternal(Set<? extends IPopup> popups) {
    propertySupport.setPropertySet(PROP_POPUPS, popups);
  }

  public void open(IPopup popup) {
    Set<IPopup> popups = getPopups();
    popups.add(popup);
    setPopupsInternal(popups);
  }

  public void close(IPopup popup) {
    Set<? extends IPopup> popups = getPopups();
    popups.remove(popup);
    setPopupsInternal(popups);
  }
}
