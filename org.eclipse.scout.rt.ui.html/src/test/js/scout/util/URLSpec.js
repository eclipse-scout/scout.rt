describe("scout.URL", function() {

  it("can parse super-simple URL", function() {
    var u = new scout.URL(encodeURI('http://www.simple.example/dir/file.html'));
    expect(u).toBeDefined();
    expect(u._baseUrlRaw).toBe('http://www.simple.example/dir/file.html');
    expect(u._queryPartRaw).toBeUndefined();
    expect(u._hashPartRaw).toBeUndefined();
    expect(u.getParameter('sort')).toBeUndefined();
  });

  it("can parse empty hash", function() {
    var u = new scout.URL(encodeURI('http://www.simple.example/dir/file.html#'));
    expect(u._hashPartRaw).toBe('');
  });

  it("can parse a moderately simple URL", function() {
    var u = new scout.URL(encodeURI('http://www.simple.example/dir/file.html?query=search&action=&sort#bottom'));
    expect(u).toBeDefined();
    expect(u._baseUrlRaw).toBe('http://www.simple.example/dir/file.html');
    expect(u._queryPartRaw).toBe('query=search&action=&sort');
    expect(u._hashPartRaw).toBe('bottom');
    expect(u.getParameter('query')).toBe('search');
    expect(u.getParameter('doesNotExist')).toBeUndefined();
    expect(u.getParameter('action')).toBe('');
    expect(u.getParameter('sort')).toBe(null);
    expect(u.getParameter('bottom')).toBeUndefined();
  });

  it("can convert the URL to string (only changed in order of arguments)", function() {
    var u = new scout.URL(encodeURI('http://www.simple.example/dir/file.html?query=search&action=&sort#bottom'));
    expect(u).toBeDefined();
    expect(u.toString()).toBe('http://www.simple.example/dir/file.html?action=&query=search&sort#bottom');
  });

  it("can handle multi-valued parameters", function() {
    var u = new scout.URL(encodeURI('http://www.simple.example/dir/file.html?query=search&action=&query&sort&query=go#bottom'));
    expect(u).toBeDefined();
    expect(u._queryPartRaw).toBe('query=search&action=&query&sort&query=go');
    expect(u.getParameter('query')).toEqual([null, 'go', 'search']);
    expect(u.toString()).toBe('http://www.simple.example/dir/file.html?action=&query&query=go&query=search&sort#bottom');
  });

  it("can add parameters", function() {
    var u = new scout.URL(encodeURI('http://www.simple.example/dir/file.html?user=hugo&submit=yes&debug#66627'));
    u.addParameter('check', 'no');
    try {
      u.addParameter();
      throw new Error('addParameter() should throw an exception');
    }
    catch (e) {
      // should throw exception
    }
    u.addParameter('', ''); // should do nothing
    u.addParameter('print');
    u.addParameter('slow', null);
    u.addParameter('x', '');
    u.addParameter('user', 'admin');
    expect(u.getParameter('check')).toBe('no');
    expect(u.getParameter('user')).toEqual(['admin', 'hugo']);
    expect(u.toString()).toBe('http://www.simple.example/dir/file.html?check=no&debug&print&slow&submit=yes&user=admin&user=hugo&x=#66627');
  });

  it("can remove parameters", function() {
    var u = new scout.URL(encodeURI('http://www.simple.example/dir/file.html?user=hugo&submit=yes&debug&user=peter#66627'));
    u.removeParameter('user');
    u.removeParameter(''); // should do nothing
    u.removeParameter('submit');
    u.removeParameter('debug');
    expect(u.getParameter('user')).toBeUndefined();
    expect(u.toString()).toBe('http://www.simple.example/dir/file.html#66627');
  });

  it("can create or replace parameters", function() {
    var u = new scout.URL(encodeURI('http://www.simple.example/dir/file.html?user=hugo&submit=yes&debug&user=peter#66627'));
    u.addParameter('user', 'test');
    u.setParameter('user', 'hans');
    u.setParameter(''); // should do nothing
    u.setParameter('submit', '');
    u.setParameter('check', null);
    expect(u.getParameter('user')).toBe('hans');
    expect(u.getParameter('submit')).toBe('');
    expect(u.getParameter('debug')).toBe(null);
    expect(u.getParameter('check')).toBe(null);
    expect(u.toString()).toBe('http://www.simple.example/dir/file.html?check&debug&submit=&user=hans#66627');
  });

  it("can handle non-ascii characters", function() {
    var u = new scout.URL(encodeURI('http://www.menükarte.example/dir/à la carte.php?query=search&wine=château lafite#\u1F603'));
    expect(u).toBeDefined();
    expect(decodeURIComponent(u._baseUrlRaw)).toBe('http://www.menükarte.example/dir/à la carte.php');
    expect(decodeURIComponent(u._queryPartRaw)).toBe('query=search&wine=château lafite');
    expect(u._hashPartRaw).toBe('%E1%BD%A03');
    expect(u.getParameter('query')).toBe('search');
    expect(u.getParameter('doesNotExist')).toBeUndefined();
    expect(u.getParameter('wine')).toBe('château lafite');
  });

});
