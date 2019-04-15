import { OutlineViewButton } from 'eclipse-scout';

export default class MyOutlineViewButton extends OutlineViewButton {

  _init(model) {
    super._init(model);
    this._setCssClass('my');
  };

}
