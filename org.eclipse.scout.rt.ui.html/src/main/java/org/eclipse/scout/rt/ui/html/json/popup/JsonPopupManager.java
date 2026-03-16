/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.popup;

import static java.util.function.Predicate.not;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.popup.IPopup;
import org.eclipse.scout.rt.client.ui.popup.PopupManager;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiSessionEvent;
import org.eclipse.scout.rt.ui.html.UiSessionListener;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;

/**
 * @since 9.0
 */
public class JsonPopupManager<T extends PopupManager> extends AbstractJsonPropertyObserver<T> {

  private P_PopupAnchorChangeListener m_popupAnchorChangeListener;
  private P_AnchorAdapterChangeListener m_anchorAdapterChangeListener;
  private final Set<IWidget> m_anchorsWithoutJsonAdapter = new HashSet<>();

  public JsonPopupManager(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "PopupManager";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<>(PopupManager.PROP_POPUPS, model, getUiSession()) {
      @Override
      protected Set<IPopup> modelValue() {
        // The json adapter of a popup uses a JsonAdapterRefProperty for its anchor property.
        // When the value of this property is resolved and an anchor is present, an exception is thrown if there is no json adapter for the anchor.
        // Therefore, remove all popups that have anchors without a json adapter.
        return getModel().getPopups().stream()
            .filter(Objects::nonNull)
            .filter(popup -> !isAnchorWithoutJsonAdapter(popup.getAnchor()))
            .collect(Collectors.toSet());
      }
    });
  }

  @Override
  protected void attachModel() {
    super.attachModel();

    // create listener initially
    if (m_popupAnchorChangeListener != null) {
      throw new IllegalStateException();
    }
    m_popupAnchorChangeListener = new P_PopupAnchorChangeListener();

    // install listener on all current popups
    Set<IPopup> popups = getModel().getPopups();
    if (!CollectionUtility.isEmpty(popups)) {
      popups.forEach(this::installPopupListeners);
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();

    // uninstall listener from all current popups
    Set<IPopup> popups = getModel().getPopups();
    if (!CollectionUtility.isEmpty(popups)) {
      popups.forEach(this::uninstallPopupListeners);
    }
  }

  @Override
  protected void handleModelPropertyChange(PropertyChangeEvent event) {
    // update listeners for all added/removed popups
    if (event.getPropertyName().equals(PopupManager.PROP_POPUPS)) {
      //noinspection unchecked
      handleModelPopupsChange((Set<IPopup>) event.getOldValue(), (Set<IPopup>) event.getNewValue());
    }
    super.handleModelPropertyChange(event);
  }

  /**
   * Updates the listeners on the given {@link IPopup}s.
   * Calls {@link #installPopupListeners(IPopup)} for all added {@link IPopup}s and {@link #uninstallPopupListeners(IPopup)} for all removed ones.
   */
  protected void handleModelPopupsChange(Set<IPopup> oldPopups, Set<IPopup> newPopups) {
    Set<IPopup> removedPopups = CollectionUtility.hashSetWithoutNullElements(oldPopups);
    if (newPopups != null) {
      removedPopups.removeAll(newPopups);
    }

    Set<IPopup> addedPopups = CollectionUtility.hashSetWithoutNullElements(newPopups);
    if (oldPopups != null) {
      addedPopups.removeAll(oldPopups);
    }

    removedPopups.forEach(this::uninstallPopupListeners);
    addedPopups.forEach(this::installPopupListeners);
  }

  /**
   * Installs the {@link #m_popupAnchorChangeListener} on the given {@link IPopup}.
   * Additionally, the anchor of the {@link IPopup} is added to the {@link #getAnchorsWithoutJsonAdapter()} (see {@link #addAnchorWithoutJsonAdapter(IWidget)}).
   */
  protected void installPopupListeners(IPopup popup) {
    if (popup == null) {
      return;
    }

    popup.addPropertyChangeListener(IPopup.PROP_ANCHOR, m_popupAnchorChangeListener);
    addAnchorWithoutJsonAdapter(popup.getAnchor());
  }

  /**
   * Uninstalls the {@link #m_popupAnchorChangeListener} from the given {@link IPopup}.
   * Additionally, the anchor of the {@link IPopup} is removed from the {@link #getAnchorsWithoutJsonAdapter()} (see {@link #removeAnchorWithoutJsonAdapter(IWidget)}).
   */
  protected void uninstallPopupListeners(IPopup popup) {
    if (popup == null) {
      return;
    }

    popup.removePropertyChangeListener(IPopup.PROP_ANCHOR, m_popupAnchorChangeListener);

    // remove observed anchor, if anchor is set and is not an anchor of any other popup
    IWidget anchor = popup.getAnchor();
    if (anchor != null && getModel().getPopups().stream()
        .filter(not(popup::equals))
        .map(IPopup::getAnchor)
        .noneMatch(anchor::equals)) {
      removeAnchorWithoutJsonAdapter(anchor);
    }
  }

  /**
   * All currently observed anchors that do not have an {@link IJsonAdapter}, used by the {@link P_AnchorAdapterChangeListener} to decide whether the json property {@link PopupManager#PROP_POPUPS} needs to be updated.
   */
  protected Set<IWidget> getAnchorsWithoutJsonAdapter() {
    return m_anchorsWithoutJsonAdapter;
  }

  /**
   * Checks whether the given {@link IWidget} is part of the {@link #getAnchorsWithoutJsonAdapter()}.
   */
  protected boolean isAnchorWithoutJsonAdapter(IWidget anchor) {
    return getAnchorsWithoutJsonAdapter().contains(anchor);
  }

  /**
   * Adds the given anchor to the {@link #getAnchorsWithoutJsonAdapter()} if there is no {@link IJsonAdapter} for it.
   * If this changes the {@link #getAnchorsWithoutJsonAdapter()} the {@link #m_anchorAdapterChangeListener} is updated if necessary.
   *
   * @return <code>true</code> if {@link #getAnchorsWithoutJsonAdapter()} changed
   */
  protected boolean addAnchorWithoutJsonAdapter(IWidget anchor) {
    if (anchor == null) {
      return false;
    }

    // there is a json adapter for the anchor -> nothing to observe
    if (JsonAdapterUtility.findChildAdapter(getUiSession().getRootJsonAdapter(), anchor) != null) {
      return false;
    }

    // anchor is already observed
    if (!getAnchorsWithoutJsonAdapter().add(anchor)) {
      return false;
    }

    // listener is created already
    if (m_anchorAdapterChangeListener != null) {
      return true;
    }

    m_anchorAdapterChangeListener = new P_AnchorAdapterChangeListener();
    getUiSession().addListener(m_anchorAdapterChangeListener, UiSessionEvent.TYPE_ADAPTER_CREATED);

    return true;
  }

  /**
   * Removes the given anchor from the {@link #getAnchorsWithoutJsonAdapter()}.
   * If this changes the {@link #getAnchorsWithoutJsonAdapter()} the {@link #m_anchorAdapterChangeListener} is updated if necessary.
   *
   * @return <code>true</code> if {@link #getAnchorsWithoutJsonAdapter()} changed
   */
  protected boolean removeAnchorWithoutJsonAdapter(IWidget anchor) {
    if (anchor == null) {
      return false;
    }

    // anchor was not observed
    if (!getAnchorsWithoutJsonAdapter().remove(anchor)) {
      return false;
    }

    // still something to observe or listener is destroyed already
    if (!getAnchorsWithoutJsonAdapter().isEmpty() || m_anchorAdapterChangeListener == null) {
      return true;
    }

    getUiSession().removeListener(m_anchorAdapterChangeListener, UiSessionEvent.TYPE_ADAPTER_CREATED);
    m_anchorAdapterChangeListener = null;

    return true;
  }

  /**
   * Updates the json property {@link PopupManager#PROP_POPUPS} of this json adapter.
   */
  protected void updatePopupsJsonProperty() {
    handleModelPropertyChange(new PropertyChangeEvent(getModel(), PopupManager.PROP_POPUPS, getModel().getPopups(), getModel().getPopups()));
  }

  /**
   * Updates the {@link #getAnchorsWithoutJsonAdapter()} when the anchor of a popup changes.
   */
  protected class P_PopupAnchorChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      ModelJobs.assertModelThread();

      IWidget oldAnchor = (IWidget) evt.getOldValue();
      IWidget newAnchor = (IWidget) evt.getNewValue();

      // remove observed anchor, if oldAnchor is part of anchorsWithoutJsonAdapter and is not an anchor of any other popup
      boolean isOldAnchorWithoutJsonAdapter = getAnchorsWithoutJsonAdapter().contains(oldAnchor);
      if (isOldAnchorWithoutJsonAdapter && getModel().getPopups().stream()
          .filter(not(evt.getSource()::equals))
          .map(IPopup::getAnchor)
          .noneMatch(oldAnchor::equals)) {
        removeAnchorWithoutJsonAdapter(oldAnchor);
      }

      addAnchorWithoutJsonAdapter(newAnchor);

      // old and new anchor do both have a json adapter or do both not have a json adapter -> no need to update the property
      boolean isNewAnchorWithoutJsonAdapter = getAnchorsWithoutJsonAdapter().contains((IWidget) evt.getNewValue());
      if (isOldAnchorWithoutJsonAdapter == isNewAnchorWithoutJsonAdapter) {
        return;
      }

      updatePopupsJsonProperty();
    }
  }

  /**
   * Updates the json property {@link PopupManager#PROP_POPUPS} (see {@link #updatePopupsJsonProperty()}) when a json adapter is created for one of the {@link #getAnchorsWithoutJsonAdapter()}.
   * This is relevant e.g. when the {@link JsonPopupManager} is processed before the json adapter of one of its popups anchors is processed which may happen e.g. when the browser is reloaded.
   * As the <code>PopupManager.ts</code> only renders newly added popups and as a formerly rendered popup will be removed when its anchor is removed, it is not necessary to listen on json adapter disposals.
   * The only time the <code>PopupManager.ts</code> will try to rerender these popups with potentially unavailable anchors is when the browser is reloaded
   * and in this case the property {@link PopupManager#PROP_POPUPS} will be sent again and is therefore correctly filtered.
   */
  protected class P_AnchorAdapterChangeListener implements UiSessionListener {

    @Override
    public void sessionChanged(UiSessionEvent e) {
      Object model = e.getJsonAdapter().getModel();
      if (!(model instanceof IWidget widget)) {
        return;
      }

      if (!removeAnchorWithoutJsonAdapter(widget)) {
        // anchor was not observed
        return;
      }

      updatePopupsJsonProperty();
    }
  }
}
