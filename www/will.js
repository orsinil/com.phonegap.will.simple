 /**
 * cordova is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) Matt Kane 2010
 * Copyright (c) 2011, IBM Corporation
 */
var WillPlugin = function() {
};

WillPlugin.open =  function(successCallback, errorCallback) {
		 cordova.exec(
					successCallback, // success callback function
					errorCallback, // error callback function
					'willPlugin', // mapped to our native Java class called "CalendarPlugin"
					'addWillEntry',
					[]
				); 
     };

module.exports = WillPlugin;