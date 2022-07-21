import WidgetModel from '../widget/WidgetModel';
import Action, {ActionStyle} from './Action';

export interface ActionModel extends WidgetModel {
  objectType?: string | { new(): Action }; // Overridden to only allow Action references
  actionStyle?: ActionStyle;
  iconId?: string;
  text?: string;
  showTooltipWhenSelected?: boolean;
}
