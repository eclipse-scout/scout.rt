package org.eclipse.scout.rt.client.ui.basic.table.control;

import org.eclipse.scout.rt.shared.data.model.IDataModel;

public interface IAnalysisTableControl extends ITableControl {

  String PROP_DATA_MODEL = "dataModel";

  IDataModel getDataModel();

//  IDataModelEntity getRootEntity(); //FIXME CGU
}
