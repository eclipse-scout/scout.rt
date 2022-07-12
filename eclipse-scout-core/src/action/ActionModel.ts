import WidgetModel from '../widget/WidgetModel';

export interface ActionModel extends WidgetModel {
  iconId?: string,
  text?: string,
  showTooltipWhenSelected?: boolean
}
