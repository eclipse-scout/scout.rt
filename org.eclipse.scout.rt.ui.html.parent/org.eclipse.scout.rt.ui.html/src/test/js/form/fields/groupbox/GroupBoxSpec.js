describe("GroupBox", function() {

  describe("_render", function() {

    var session;
    var groupBox;
    var model = {
          label: "fooBar",
          gridData : {
            x : 0,
            y : 0
          },
          gridColumnCount : 2,
          parent : {
            objectType : 'GroupBox'
          }
    };

    beforeEach(function() {
      setFixtures(sandbox());
      session = new scout.Session($('#sandbox'), '1.1');
      groupBox = new scout.GroupBox();
      groupBox.init(model, session);
    });

    it("adds group-box div when label is set", function() {
      groupBox._render($('#sandbox'));
      expect($('#sandbox')).toContainElement('div.group-box');
      expect($('#sandbox')).toContainElement('div.group-box-title');
    });
  });

});
