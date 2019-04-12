import Widget from './Widget';

export default class NullWidget extends Widget {
  constructor() {
    super();
    this._addWidgetProperties(['childWidget']);
  }

  setChildWidget(childWidget) {
    this.setProperty('childWidget', childWidget);
  };
}
