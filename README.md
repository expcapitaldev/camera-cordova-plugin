
Plugin takes picture from native camera.

You shoud declare CustomCamera variable and call:
CustomCamera.takePicture(success, failure, [title, subtitle, cancel, message, type])

type could be: PASSPORT, A4, LICENCE, NONE
Returns absolute file path for stored image in successCallback. Image is stored in
internal storage so no extra permissions needed.

In case of error, errorCallback is triggered with parameter describind whats happened.
Errors could be:
NO_PERMISSION - user did not grant camera permission
CANCELLED - user closed the flow 
FILE_NOT_FOUND - problem with creating file for storing the result. Could be out of memory in internal storage.
ERROR_ACCESSING_FILE - problem while writing file. Could be out of memory in internal storage.
CAMERA_ERROR - any problem with camera initialisation. For instance, there is no camera on device.

In order to run test project, you need to add plugin and build the project.
⇒  cordova plugin add --link ../camera-cordova-plugin
⇒  ionic cordova emulate android

Plugin has 2 dependencies, please keep attention on version of support library. You can update it if needed:
    'com.android.support:appcompat-v7:27.1.1'
    'com.otaliastudios:cameraview:1.5.1'
# camera-cordova-plugin
