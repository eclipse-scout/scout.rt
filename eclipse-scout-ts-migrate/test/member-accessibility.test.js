import {mockPluginParams} from './test-utils.js';
import memberAccessModifierPlugin from '../src/memberAccessModifierPlugin.js';
import {crlfToLf} from '../src/common.js';

describe('member-access-modifier plugin', () => {
  it('adds access modifiers to class elements', async () => {
    let text = `\
class C {
    _protectedProperty: any;
    static _protectedStaticProperty: any;
    _protectedMethod() {}
    get _protectedGetter() {}
    set _protectedSetter(v) {}
    public _looksProtected() {}
}`;
    let result = await memberAccessModifierPlugin.run(
      mockPluginParams({text, fileName: 'file.ts', options: {}})
    );

    result = crlfToLf(result);
    expect(result).toBe(`\
class C {
    protected _protectedProperty: any;
    protected static _protectedStaticProperty: any;
    protected _protectedMethod() {}
    protected get _protectedGetter() {}
    protected set _protectedSetter(v) {}
    public _looksProtected() {}
}`);
  });
});
