// eslint-disable-next-line max-classes-per-file
import {Action, Form, GroupBox, Icon, icons, KeyStrokeFirePolicyExt, Menu, scout} from '../index';
import {ActionStyleEnum, EnumObject, EnumType, IActionStyle} from './Action';
import {ActionModel} from './ActionModel';

enum ActionStyleEnumExt {
  NEW_STYLE = 10
}

interface IActionStyleExt extends IActionStyle {
  NEW_STYLE: 10
}

export default class ActionExt extends Action {
  // @ts-ignore
  actionStyle: EnumObject<typeof ActionExt.ActionStyle>;
  // @ts-ignore
  actionStyleEnum: ActionStyleEnum | ActionStyleEnumExt;

  // @ts-ignore
  actionStyleInterface: EnumType<IActionStyleExt>[keyof EnumType<IActionStyleExt>];

  // @ts-ignore
  literal: 1 | 2 | 3;

  // @ts-ignore
  textPosition: typeof ActionExt.TextPosition[keyof typeof ActionExt.TextPosition];

  keyStrokeFirePolicy: KeyStrokeFirePolicyExt;

  constructor() {
    super();
    this.actionStyle = ActionExt.ActionStyle.NEW_POSITION;
    this.actionStyleEnum = ActionStyleEnumExt.NEW_STYLE;
    this.actionStyleInterface = ActionExt.ActionStyleVar.NEW_STYLE;
    this.textPosition = ActionExt.TextPosition.NEW_POSITION;
    this.keyStrokeFirePolicy = KeyStrokeFirePolicyExt.NEW_VALUE;
  }

  static ActionStyle = {
    ...Action.ActionStyle,
    NEW_POSITION: 'abc'
  } as const;

  static TextPosition = {
    ...Action.TextPosition,
    NEW_POSITION: 'abc'
  } as const;

  static ActionStyleVar:IActionStyleExt = {
    NEW_STYLE: 10,
    DEFAULT: 0,
    BUTTON: 1
  };

  set iconId(iconId) {
    this.setProperty('iconId', iconId);
  }

  _init(model: ActionModel) {
    super._init(model);
    console.log('ActionStyle:' + this.actionStyle);
    console.log('ActionStyleEnum:' + this.actionStyleEnum);
    console.log('ActionStyleInterface:' + this.actionStyleInterface);
    console.log('keyStrokeFirePolicy:' + this.keyStrokeFirePolicy);

    let icon = scout.create(Icon, {
      parent: this,
      iconDesc: icons.AVG
    });
    console.log(icon.iconDesc);

    icon.model.iconDesc = 'asdf';
    // icon.model = {parent: this};
  }

  _doAction() {
    super._doAction();

    let form = scout.create(Form, {
      parent: this.parent,
      title: 'Woohooo!',
      rootGroupBox: {
        objectType: GroupBox,
        borderDecoration: GroupBox.BorderDecoration.EMPTY,
        menus: [
          {
            objectType: Menu,
            id: 'HiMenu',
            text: 'Hi',
            iconId: icons.TARGET
          }
        ]
      }
    });
    form.open();

    let actionExt = scout.create(ActionExt, {
      parent: this
    });
  }
}
