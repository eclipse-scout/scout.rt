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

import java.util.Set;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

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
