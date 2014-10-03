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
package org.eclipse.scout.rt.ui.html.json.form.fields.smartfield;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonSmartField extends JsonValueField<ISmartField> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonSmartField.class);

  private static final int MAX_OPTIONS = 100;

  public JsonSmartField(ISmartField model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  public String getObjectType() {
    return "SmartField";
  }

  @Override
  public JSONObject toJson() {
    return addOptions(super.toJson());
  }

  private JSONObject addOptions(JSONObject json) {
    // TODO AWE: (smartfield) überlegen ob wir die optionen wirklich hier laden wollen oder besser erst,
    // wenn das popup im UI zum ersten mal geöffnet wird.
    final Holder<List<? extends ILookupRow<?>>> holder = new Holder<>();
    new ClientSyncJob("loadOptions", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        @SuppressWarnings("unchecked")
        List<? extends ILookupRow<?>> list = getModel().callBrowseLookup(IContentAssistField.BROWSE_ALL_TEXT, MAX_OPTIONS);
        holder.setValue(list);
      }
    }.runNow(new NullProgressMonitor());
    JSONArray optionsArray = new JSONArray();
    for (ILookupRow<?> lr : holder.getValue()) {
      optionsArray.put(lr.getText());
    }
    return putProperty(json, "options", optionsArray);
  }
}
