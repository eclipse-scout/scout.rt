// eslint-disable-next-line max-classes-per-file
import {Action, Form, GroupBox, Icon, icons, KeyStrokeFirePolicyExt, Menu, scout} from '../index';
import {ActionStyleEnum, EnumObject, EnumType, IActionStyle} from './Action';
import {ActionModel} from './ActionModel';
import {ActionExtModel} from './ActionExtModel';
import TableMenuType from '../table/TableMenuType';
import MenuModel from '../menu/MenuModel';

enum ActionStyleEnumExt {
  NEW_STYLE = 10
}

interface IActionStyleExt extends IActionStyle {
  NEW_STYLE: 10;
}

export type ActionStyleExt = EnumObject<typeof ActionExt.ActionStyle>;

type ActionTextPositionKey = keyof typeof ActionExt.TextPosition;
export type ActionTextPositionExt = typeof ActionExt.TextPosition[ActionTextPositionKey];

export default class ActionExt extends Action {
  // @ts-ignore
  model: ActionExtModel;

  // @ts-ignore
  actionStyle: ActionStyleExt;
  // @ts-ignore
  actionStyleEnum: ActionStyleEnum | ActionStyleEnumExt;

  // @ts-ignore
  actionStyleInterface: EnumType<IActionStyleExt>[keyof EnumType<IActionStyleExt>];

  // @ts-ignore
  literal: 1 | 2 | 3;

  // @ts-ignore
  textPosition: ActionTextPositionExt;

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
    NEW_POSITION: 10
  } as const;

  static TextPosition = {
    ...Action.TextPosition,
    NEW_POSITION: 'abc'
  } as const;

  static ActionStyleVar: IActionStyleExt = {
    NEW_STYLE: 10,
    DEFAULT: 0,
    BUTTON: 1
  };

  set iconId(iconId) {
    this.setProperty('iconId', iconId);
  }

  setActionStyle(actionStyle: ActionStyleExt) {
    // @ts-ignore
    super.setActionStyle(actionStyle);
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

    this.actionStyle2 = Action.ActionStyle.BUTTON;
    this.textPosition = ActionExt.TextPosition.NEW_POSITION;
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
      parent: this,
      actionStyle: ActionExt.ActionStyle.NEW_POSITION
    });

    let menu2 = scout.create(Menu, {
      parent: this,
      text: 'Other menu'
    });

    let icon = scout.create(Icon, {
      parent: this,
      iconDesc: icons.AVG
    });

    let menu = scout.create(Menu, {
      parent: this,
      menuTypes: [TableMenuType.SingleSelection],
      childActions: [
        menu2, {
          objectType: Menu
        }
      ]
    });
  }
}
