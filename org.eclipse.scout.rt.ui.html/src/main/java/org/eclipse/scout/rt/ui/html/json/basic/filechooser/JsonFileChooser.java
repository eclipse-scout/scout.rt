/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.basic.filechooser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.basic.filechooser.FileChooserEvent;
import org.eclipse.scout.rt.client.ui.basic.filechooser.FileChooserListener;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonFileChooser<FILE_CHOOSER extends IFileChooser> extends AbstractJsonAdapter<FILE_CHOOSER> implements IBinaryResourceConsumer {

  // UI events
  private static final String EVENT_CANCEL = "cancel";

  private FileChooserListener m_fileChooserListener;

  public JsonFileChooser(FILE_CHOOSER model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "FileChooser";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_fileChooserListener != null) {
      throw new IllegalStateException();
    }
    m_fileChooserListener = new P_FileChooserListener();
    getModel().addFileChooserListener(m_fileChooserListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_fileChooserListener == null) {
      throw new IllegalStateException();
    }
    getModel().removeFileChooserListener(m_fileChooserListener);
    m_fileChooserListener = null;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putProperty(json, "multiSelect", getModel().isMultiSelect());
    putProperty(json, "acceptTypes", new JSONArray(collectAcceptTypes()));
    putProperty(json, "maximumUploadSize", getModel().getMaximumUploadSize());
    return json;
  }

  protected Set<String> collectAcceptTypes() {
    return BEANS.get(JsonFileChooserAcceptAttributeBuilder.class)
        .withTypes(getModel().getFileExtensions())
        .build();
  }

  @SuppressWarnings("SwitchStatementWithTooFewBranches")
  protected void handleModelFileChooserEvent(FileChooserEvent event) {
    switch (event.getType()) {
      case FileChooserEvent.TYPE_CLOSED:
        handleModelClosed();
        break;
      default:
        // NOP
    }
  }

  protected void handleModelClosed() {
    // ==> Same code in JsonForm, JsonMessageBox! <==

    // This removes the adapter from the adapter registry and the current JSON response.
    // Also, all events for this adapter in the current response are removed.
    dispose();

    // JSON adapter is now disposed. To dispose it on the UI, too, we have to send an explicit
    // event to the UI session. We do NOT send a "closed" event for this adapter, because the
    // adapter may not have been sent to the UI (e.g. opening and closing a file chooser in the
    // same request). If we would send a "closed" event for an adapter that does not exist on
    // the UI, an error would be thrown, because the previous dispose() call removed the adapter
    // from the response, and it will  never be sent to the UI. Only the 'disposeAdapter' event
    // handler on the session can handle that situation (see Session.js).
    getUiSession().sendDisposeAdapterEvent(this);
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_CANCEL.equals(event.getType())) {
      handleUiCancel(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiCancel(JsonEvent event) {
    getModel().getUIFacade().setResultFromUI(null);
  }

  @Override
  public Collection<String> getAcceptedUploadFileExtensions() {
    return getModel().getFileExtensions();
  }
  @Override
  public void consumeBinaryResource(List<BinaryResource> binaryResources, Map<String, String> uploadProperties) {
    getModel().getUIFacade().setResultFromUI(binaryResources);
  }

  @Override
  public long getMaximumUploadSize() {
    return getModel().getMaximumUploadSize();
  }

  protected class P_FileChooserListener implements FileChooserListener {

    @Override
    public void fileChooserChanged(FileChooserEvent e) {
      ModelJobs.assertModelThread();
      handleModelFileChooserEvent(e);
    }
  }
}
