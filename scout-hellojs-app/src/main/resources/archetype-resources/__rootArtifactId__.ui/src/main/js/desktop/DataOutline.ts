import {Outline, OutlineModel} from '@eclipse-scout/core';
import DataOutlineModel, {DataOutlineWidgetMap} from './DataOutlineModel';

export class DataOutline extends Outline {
  declare widgetMap: DataOutlineWidgetMap;

  protected override _jsonModel(): OutlineModel {
    return DataOutlineModel();
  }
}
