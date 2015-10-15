describe("scout.defaultValues", function() {

  afterEach(function() {
    // Reload default values to not influence other tests
    scout.defaultValues.bootstrap();
  });

  describe("_loadDefaultsConfiguration", function() {

    it("can load invalid configurations", function() {
      expect(function() {
        scout.defaultValues._loadDefaultsConfiguration();
      }).toThrow();
      scout.defaultValues._loadDefaultsConfiguration({});
      scout.defaultValues._loadDefaultsConfiguration({
        "defaults": {}
      });
      scout.defaultValues._loadDefaultsConfiguration({
        "objectTypeHierarchy": {}
      });
      expect(function() {
        scout.defaultValues._loadDefaultsConfiguration({
          "objectTypeHierarchy": {
            "FormField": {
              "TableField": null,
              "ValueField": {
                "TableField": null
              }
            }
          }
        });
      }).toThrow();
    });

  });

  describe("applyTo", function() {

    it("can apply default values to JSON", function() {
      var config = {
        "defaults": {
          "FormField": {
            "width": 10,
            "alignment": -1,
            "keyStrokes": [],
            "gridData": {
              "x": 0,
              "y": 0
            }
          },
          "NumberField": {
            "alignment": 1
          }
        },
        "objectTypeHierarchy": {
          "FormField": {
            "ValueField": {
              "NumberField": null,
              "SmartField": null
            }
          }
        }
      };
      scout.defaultValues._loadDefaultsConfiguration(config);

      var testObjects = [{ // [0]
        "id": "2",
        "objectType": "SmartField"
      }, { // [1]
        "id": "3",
        "objectType": "SmartField",
        "width": 20
      }, { // [2]
        "id": "4",
        "objectType": "NumberField",
        "width": 20
      }, { // [3]
        "id": "100",
        "plainValue": {}
      }, { // [4]
        "id": "103",
        "objectType": "SmartField",
        "gridData": "77"
      }, { // [5]
        "id": "104",
        "objectType": "SmartField",
        "gridData": {
          y: 5
        }
      }];
      scout.defaultValues.applyTo(testObjects);

      expect(testObjects[0].width).toBe(10);
      expect(testObjects[0].x).toBe(undefined);
      expect(testObjects[1].width).toBe(20);
      expect(testObjects[1].alignment).toBe(-1);
      expect(testObjects[2].alignment).toBe(1);
      expect(testObjects[2].width).toBe(20);
      expect(testObjects[3].gridData).toBe(undefined);
      expect(testObjects[4].gridData).toBe('77');
      expect(testObjects[5].gridData.x).toBe(0);
      expect(testObjects[5].gridData.y).toBe(5);
    });

    it("can apply default values to JSON considering the model variant", function() {
      var config = {
        "defaults": {
          "FormField": {
            "enabled": true
          },
          "TableField.Custom": {
            "enabled": false,
            "borderDecoration": "auto"
          }
        },
        "objectTypeHierarchy": {
          "FormField": {
            "TableField": null
          }
        }
      };
      scout.defaultValues._loadDefaultsConfiguration(config);

      var testObjects = [{ // [0]
        "id": "1",
        "objectType": "FormField",
        "visible": true,
        "borderDecoration": "auto"
      }, { // [1]
        "id": "2",
        "objectType": "TableField",
        "visible": true,
        "borderDecoration": "auto"
      }, { // [2]
        "id": "3",
        "objectType": "FormField.Custom",
        "visible": true,
        "borderDecoration": "auto"
      }, { // [3]
        "id": "4",
        "objectType": "TableField.Custom",
        "enabled": true,
        "visible": true
      }];
      scout.defaultValues.applyTo(testObjects);

      expect(testObjects[0].enabled).toBe(true);
      expect(testObjects[0].visible).toBe(true);
      expect(testObjects[0].borderDecoration).toBe("auto");
      expect(testObjects[1].enabled).toBe(true);
      expect(testObjects[1].visible).toBe(true);
      expect(testObjects[1].borderDecoration).toBe("auto");
      expect(testObjects[2].enabled).toBe(true);
      expect(testObjects[2].visible).toBe(true);
      expect(testObjects[2].borderDecoration).toBe("auto");
      expect(testObjects[3].enabled).toBe(true);
      expect(testObjects[3].visible).toBe(true);
      expect(testObjects[3].borderDecoration).toBe("auto");
    });

    it("copies default values 'by value'", function() {
      var config = {
        "defaults": {
          "Table": {
            rows: []
          }
        }
      };
      scout.defaultValues._loadDefaultsConfiguration(config);

      var testObjects = [{
        "id": "1",
        "objectType": "Table"
      }, {
        "id": "2",
        "objectType": "Table"
      }, {
        "id": "3",
        "objectType": "Table",
        rows: ["three"]
      }];
      scout.defaultValues.applyTo(testObjects);

      expect(testObjects[0].rows).toEqual([]);
      expect(testObjects[1].rows).toEqual([]);
      expect(testObjects[2].rows).toEqual(['three']);

      var testRows = testObjects[0].rows;
      testRows.push('one');

      expect(testObjects[0].rows).toEqual(['one']);
      expect(testObjects[1].rows).toEqual([]);
      expect(testObjects[2].rows).toEqual(['three']);
      expect(scout.defaultValues._defaults.Table.rows).toEqual([]);
    });

  });

});
