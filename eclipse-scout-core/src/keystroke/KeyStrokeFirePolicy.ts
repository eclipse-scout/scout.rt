// eslint-disable-next-line max-classes-per-file
export class Enum {
  value: number | string;

  constructor(value) {
    this.value = value;
  }

  toString() {
    return this.constructor.name + ': ' + this.value;
  }
}

export default class KeyStrokeFirePolicy extends Enum {
  static ACCESSIBLE_ONLY = new KeyStrokeFirePolicy(0);
  static ALWAYS = new KeyStrokeFirePolicy(1);
}
