
#import "CustomCamera.h"
#import "CCCameraOptions.h"
#import "CCCameraViewController.h"
#import <AVFoundation/AVFoundation.h>

typedef void (^CCVoidBlock) ();

#pragma mark - NSArray Utils

@implementation NSArray (Utils)

- (NSString *)cc_stringValueAtIndex:(NSUInteger)index {
    id object = nil;
    if (index < self.count) {
        object = self[index];
    }
    
    if ([object isKindOfClass:[NSString class]]) {
        return (NSString *)object;
    }
    return nil;
}

@end

#pragma mark - CustomCamera Private interface

@interface CustomCamera () <CCCameraViewControllerDelegate>

@property (strong, nonatomic) CCCameraViewController *cameraController;

@end

#pragma mark - CustomCamera Implementation

@implementation CustomCamera

// MARK: - Public methods

- (void)takePicture:(CDVInvokedUrlCommand*)command {
    __weak CustomCamera* weakSelf = self;

    [self.commandDelegate runInBackground:^{
        BOOL hasCamera = [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera];
        if (!hasCamera) {
            NSLog(@"CustomCamera.takePicture: source type %lu not available.", UIImagePickerControllerSourceTypeCamera);
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No camera available"];
            [weakSelf completePluginWithResult:result callbackId:command.callbackId];
            return;
        }

        CCCameraOptions *cameraOptions = [self cameraOptionsTakePictureArguments:command];
        
        // Validate the app has permission to access the camera
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
             dispatch_async(dispatch_get_main_queue(), ^{
                 if (!granted) {
                     [weakSelf showPermissionsAlert:command];
                 } else {
                     [weakSelf showCameraControllerForCallback:command.callbackId options: cameraOptions];
                 }
             });
        }];
    }];
}

// MARK: - Private methods

- (CCCameraOptions *)cameraOptionsTakePictureArguments:(CDVInvokedUrlCommand *)command {
    CCCameraOptions* options = [CCCameraOptions new];

    NSArray *params = [command argumentAtIndex:0 withDefault:@[]];
    if (params && params.count > 0) {
        options.title = [params cc_stringValueAtIndex:CCParamsIndexTitle];
        options.subtitle = [params cc_stringValueAtIndex:CCParamsIndexSubtitle];
        options.message = [params cc_stringValueAtIndex:CCParamsIndexMessage];
        options.viewFinderType = [params cc_stringValueAtIndex:CCParamsIndexViewFinderType];
        
        options.cancelText = [params cc_stringValueAtIndex:CCParamsIndexCancelText];
        options.processingText = [params cc_stringValueAtIndex:CCParamsIndexProcessingText];
        options.takingText = [params cc_stringValueAtIndex:CCParamsIndexTakingText];
    }
    return options;
}

- (void)showPermissionsAlert: (CDVInvokedUrlCommand*)command {
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleDisplayName"]
                                                                             message:NSLocalizedString(@"Access to the camera has been prohibited; please enable it in the Settings app to continue.", nil)
                                                                      preferredStyle:UIAlertControllerStyleAlert];
    
    __weak CustomCamera* weakSelf = self;
    [alertController addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"OK", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [weakSelf sendNoPermissionResult:command.callbackId];
    }]];
    [alertController addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Settings", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString] options:@{} completionHandler:nil];
        [weakSelf sendNoPermissionResult:command.callbackId];
    }]];
    [self.viewController presentViewController:alertController animated:YES completion:nil];
}

- (void)showCameraControllerForCallback:(NSString *)callbackId options:(CCCameraOptions *)options {
    self.cameraController = [[CCCameraViewController alloc] initWithNibName:NSStringFromClass([CCCameraViewController class]) bundle:nil];
    self.cameraController.delegate = self;
    self.cameraController.options = options;
    self.cameraController.callbackId = callbackId;
    
    // we need to capture this state for memory warnings that dealloc this object
    self.cameraController.webView = self.webView;
    
    self.cameraController.modalPresentationStyle = UIModalPresentationCurrentContext;
    [self.viewController presentViewController:self.cameraController animated:YES completion:nil];
}

- (void)sendNoPermissionResult:(NSString *)callbackId {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"has no access to camera"];   // error callback expects string ATM
    [self completePluginWithResult:result callbackId:callbackId];
}

- (void)completePluginWithResult:(CDVPluginResult *)result callbackId:(NSString *)callbackId {
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    self.cameraController = nil;
}

// MARK: - CCCameraViewControllerDelegate

- (void)cameraControllerDidUsePhoto:(UIImage *)photo {
    __weak CustomCamera *weakSelf = self;
    CCVoidBlock completionBlock = ^{
        __block CDVPluginResult *result = nil;

        NSString *imageBase64 = [UIImageJPEGRepresentation(photo, 0.5) base64EncodedStringWithOptions:0];
        if (imageBase64)  {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:imageBase64];
        }
        [weakSelf completePluginWithResult:result callbackId:weakSelf.cameraController.callbackId];
    };

    [self.viewController dismissViewControllerAnimated:YES completion:completionBlock];
}

- (void)cameraControllerDidCancel:(CCCameraViewController *)controller {
    __weak CustomCamera *weakSelf = self;
    CCVoidBlock completionBlock = ^{
        CDVPluginResult *result;
        if ([AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo] != AVAuthorizationStatusAuthorized) {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"has no access to camera"];
        } else {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No Image Selected"];
        }
        [weakSelf completePluginWithResult:result callbackId:weakSelf.cameraController.callbackId];
    };

    [self.viewController dismissViewControllerAnimated:YES completion:completionBlock];
}

@end
