import WidgetsApp from '../../src/widgets/WidgetsApp';
import { Session, NullWidget } from 'eclipse-scout';

describe('WidgetsApp', () => {

  it('_createDesktop', () => {
    var app = new WidgetsApp();
    var nullWidget = new NullWidget();
    nullWidget.session = new Session();
    var desktop = app._createDesktop(nullWidget);
    expect(desktop.viewButtons.length).toBe(2);
  });

});
