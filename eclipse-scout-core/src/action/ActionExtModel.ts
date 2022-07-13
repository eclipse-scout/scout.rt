import {ActionModel} from './ActionModel';
import {ActionStyleExt} from './ActionExt';

// @ts-ignore
export interface ActionExtModel extends ActionModel {
  actionStyle?: ActionStyleExt;
}
