package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.platform.service.IService;
import org.json.JSONObject;

public interface IDefaultValuesFilterService extends IService {

  void filter(JSONObject json);

  void filter(JSONObject json, String objectType);
}
