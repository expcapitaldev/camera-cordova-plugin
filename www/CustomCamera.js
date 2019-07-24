/*
  Custom implementation of camera with supported border.
 */

function _cordova_exec(actionName, successCallback, errorCallback, options) {
  cordova.exec(successCallback, errorCallback, "CustomCamera", actionName, (options ? [options] : []));
}

module.exports = {
    
  /*
   / Takes picture from native camera. Returns Base64 jpeg.
   / CustomCamera.takePicture(success, failure, [title, subtitle, cancel, message, type])
   /
   / Type could be: PASSPORT, A4, LICENCE, NONE, FACE
   / Returns absolute file path for stored image in successCallback. Image is stored in
   / internal storage so no extra permissions needed.
   / 
   / In case error, errorCallback is triggered with parameter describind whats happened.
   / Errors could be:
   /   NO_PERMISSION - user did not grant camera permission
   /   CANCELLED - user closed the flow 
   /   FILE_NOT_FOUND - problem with creating file for storing the result. Could be out of memory in internal storage.
   /   ERROR_ACCESSING_FILE - problem while writing file. Could be out of memory in internal storage.
   /   CAMERA_ERROR - any problem with camera initialisation. For instance, there is no camera on device.
   / 
  */
  takePicture: function(successCallback, errorCallback, options) {
    _cordova_exec("takePicture", successCallback, errorCallback, options);
  },
    
};
