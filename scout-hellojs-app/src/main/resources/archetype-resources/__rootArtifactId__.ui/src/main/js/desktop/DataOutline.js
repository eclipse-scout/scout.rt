import {Outline, models} from '@eclipse-scout/core';
import DataOutlineModel from './DataOutlineModel';

export default class DataOutline extends Outline {

  constructor() {
    super();
  }


  _jsonModel() {
    return models.get(DataOutlineModel);
  }
}
