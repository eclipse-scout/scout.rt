describe("TableMenuItemsOrder", function() {

//  beforeEach(function() {
//    $.ajax({
//      async: false,
//      type: 'GET',
//      dataType: 'text',
//      contentType: 'text/plain; charset=UTF-8',
//      cache: false,
//      url: 'WebContent/res/defaultValues.json',
//      data: ''
//    }).done(function(data) {
//      var dataWithoutComments = removeCommentsFromJson(data),
//        dataObj = JSON.parse(dataWithoutComments);
//      scout.defaultValues._loadDefaultsConfiguration(dataObj);
//    }).fail(function(jqXHR, textStatus, errorThrown) {
//      throw new Error('Error while loading default values: ' + errorThrown);
//    });
//  });

  it("createSeparator", function() {
    var separator = scout.TableMenuItemsOrder.createSeparator();
    expect(separator.separator).toBe(true);
    expect(separator.session).toBeTruthy();
    expect(separator.visible).toBe(true);
    expect(separator.enabled).toBe(true);
    expect(separator.selected).toBe(false);
  });

});
