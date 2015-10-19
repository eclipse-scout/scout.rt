scout.aggregation = {

    sumStart: function(){
      return 0;
    },

    sumStep: function(currentState, newVal) {
      var val = newVal || 0;
      currentState += val;
      return currentState;
    },

    sumFinish: function(currentState) {
      return currentState;
    },

    sumSymbol: '\u03a3',

    avgStart: function(){
      return {sum: 0, count: 0};
    },

    avgStep: function(currentState, newVal) {
      var val = newVal || 0;
      if(newVal || newVal === 0){
        currentState.sum += val;
        currentState.count += 1;
      }
      return currentState;
    },

    avgFinish: function(currentState) {
      if(currentState.count && currentState.count > 0){
        return (currentState.sum * 1.0) / currentState.count;
      }
    },

    avgSymbol: '\u00D8',

    minStart: function(){
      return null;
    },

    minStep: function(currentState, newVal) {
      if(newVal || newVal === 0){
        if(currentState || currentState === 0){
          if(newVal < currentState){
            currentState = newVal;
          }
        }
        else{
          currentState = newVal;
        }
      }
      return currentState;
    },

    minFinish: function(currentState) {
      return currentState;
    },

    minSymbol: '\uf077',

    maxStart: function(){
      return null;
    },

    maxStep: function(currentState, newVal) {
      if(newVal || newVal === 0){
        if(currentState || currentState === 0){
          if(newVal > currentState){
            currentState = newVal;
          }
        }
        else{
          currentState = newVal;
        }
      }
      return currentState;
    },

    maxFinish: function(currentState) {
      return currentState;
    },

    maxSymbol: '\uf078'

};
