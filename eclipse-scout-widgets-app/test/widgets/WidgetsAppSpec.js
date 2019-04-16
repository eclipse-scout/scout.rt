import WidgetsApp from '../../src/widgets/WidgetsApp';
import { NullWidget } from 'eclipse-scout';

describe('WidgetsApp', () => {

  it('_createDesktop', () => {
    var app = new WidgetsApp();
    var nullWidget = new NullWidget();
    app._createDesktop(nullWidget);
    expect(app.desktop.viewButtons.length).toBe(2);
  });

});
