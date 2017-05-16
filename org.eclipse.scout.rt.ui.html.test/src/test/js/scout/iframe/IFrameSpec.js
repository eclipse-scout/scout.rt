describe('IFrame', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('setLocation', function() {
    it('sets the location of the iframe', function() {
      var iframe = scout.create('IFrame', {
        parent: session.desktop
      });
      iframe.render();
      expect(iframe.location).toBe(null);
      expect(iframe.$iframe.attr('src')).toBe('about:blank');

      iframe.setLocation('http://www.bing.com');
      expect(iframe.location).toBe('http://www.bing.com');
      expect(iframe.$iframe.attr('src')).toBe('http://www.bing.com');
    });

    it('sets the location to about:blank if location is empty', function() {
      var iframe = scout.create('IFrame', {
        parent: session.desktop,
        location: 'http://www.bing.com'
      });
      iframe.render();
      expect(iframe.location).toBe('http://www.bing.com');
      expect(iframe.$iframe.attr('src')).toBe('http://www.bing.com');

      iframe.setLocation(null);
      expect(iframe.location).toBe(null);
      expect(iframe.$iframe.attr('src')).toBe('about:blank');
    });
  });
});
