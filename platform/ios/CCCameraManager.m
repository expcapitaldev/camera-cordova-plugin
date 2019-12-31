
#import "CCCameraManager.h"
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

// MARK: - CCCameraManager Private interface

@interface CCCameraManager() <AVCapturePhotoCaptureDelegate>

@property (strong, nonatomic) AVCaptureSession *captureSession;
@property (strong, nonatomic) AVCapturePhotoOutput *photoOutput;
@property (strong, nonatomic) AVCaptureVideoPreviewLayer *previewLayer;
@property (assign, nonatomic) CCCameraType cameraType;

@property (weak, nonatomic) UIView *parentView;
@property (copy, nonatomic) CCPhotoCaptureBlock photoCaptureBlock;

@end

// MARK: - CCCameraManager implementation

@implementation CCCameraManager

+ (dispatch_queue_t)sharedCameraQueue {
    static dispatch_queue_t queue;
    static dispatch_once_t predicate;
    
    dispatch_queue_attr_t queueAttributes = dispatch_queue_attr_make_with_qos_class(DISPATCH_QUEUE_SERIAL, QOS_CLASS_USER_INITIATED, 0);
    
    dispatch_once(&predicate, ^{
        queue = dispatch_queue_create("com.expcapital.customcamera.queue", queueAttributes);
    });
    return queue;
}

// MARK: - Public methods

- (instancetype)init {
    return [self initWithCamera:CCCameraTypeBack];
}

- (instancetype)initWithCamera:(CCCameraType)cameraType {
    if (self = [super init]) {
        self.cameraType = cameraType;
        [self setupCaptureSession];
    }
    return self;
}

- (void)addPreviewLayerToView:(UIView *)view {
    if (!self.parentView) {
        [self updatePreviewLayerParentView:view];
    } else if (self.parentView != view) {
        if (!self.previewLayer.superlayer) {
            [self.previewLayer removeFromSuperlayer];
        }
        [self updatePreviewLayerParentView:view];
    }
}

- (void)updatePreviewLayerLayout {
    if (self.previewLayer.frame.size.width != self.parentView.bounds.size.width || self.previewLayer.frame.size.height != self.parentView.bounds.size.height) {
        self.previewLayer.frame = self.parentView.bounds;
    }
}

- (void)startSession {
    __weak CCCameraManager *weakSelf = self;
    dispatch_async([CCCameraManager sharedCameraQueue], ^{
        if (![weakSelf.captureSession isRunning]) {
            [weakSelf.captureSession startRunning];
        }
    });
}

- (void)stopSession {
    __weak CCCameraManager *weakSelf = self;
    dispatch_async([CCCameraManager sharedCameraQueue], ^{
        if ([weakSelf.captureSession isRunning]) {
            [weakSelf.captureSession stopRunning];
        }
    });
}

- (void)capturePhoto:(CCPhotoCaptureBlock)completion {
    if (self.photoCaptureBlock) {
        NSLog(@"%@: is already capturing photo", [self class]);
    } else {
        self.photoCaptureBlock = completion;
        
        AVCapturePhotoSettings *settings = [AVCapturePhotoSettings photoSettingsWithFormat:@{AVVideoCodecKey: AVVideoCodecTypeJPEG}];
        [self.photoOutput capturePhotoWithSettings:settings delegate:self];
    }
}

// MARK: - AVCapturePhotoCaptureDelegate

- (void)captureOutput:(AVCapturePhotoOutput *)output didFinishProcessingPhoto:(AVCapturePhoto *)photo error:(nullable NSError *)error {
    NSData *imageData = photo.fileDataRepresentation;
    if (imageData && self.photoCaptureBlock) {
        UIImage *image = [UIImage imageWithData:imageData];
        self.photoCaptureBlock(image, nil);
    } else if (!imageData && self.photoCaptureBlock) {
        self.photoCaptureBlock(nil, error);
    }
    self.photoCaptureBlock = nil;
}

// MARK: - Private methods

- (void)setupCaptureSession {
    self.captureSession = [AVCaptureSession new];
    self.captureSession.sessionPreset = AVCaptureSessionPresetPhoto;
    
    AVCaptureDevice *cameraDevice = [self cameraWithType:self.cameraType];
    if (!cameraDevice) {
        NSLog(@"Unable to access camera!");
        return;
    }
    
    NSError *error;
    AVCaptureDeviceInput *input = [AVCaptureDeviceInput deviceInputWithDevice:cameraDevice error:&error];
    if (error) {
        NSLog(@"Failed to initialize back camera: %@", error.description);
    }
    
    self.photoOutput = [AVCapturePhotoOutput new];
    if ([self.captureSession canAddInput:input] && [self.captureSession canAddOutput:self.photoOutput]) {
        [self.captureSession addInput:input];
        [self.captureSession addOutput:self.photoOutput];
    }
}

- (AVCaptureDevice *)cameraWithType:(CCCameraType)type {
    AVCaptureDevicePosition position = (type == CCCameraTypeBack) ? AVCaptureDevicePositionBack : AVCaptureDevicePositionFront;
    return [AVCaptureDeviceDiscoverySession discoverySessionWithDeviceTypes:@[AVCaptureDeviceTypeBuiltInWideAngleCamera]
                                                           mediaType:AVMediaTypeVideo
                                                            position:position].devices.firstObject;
}

- (void)setupLivePreview {
    if (!self.captureSession) {
        NSLog(@"CaptureSession is not configured");
        return;
    }
    
    self.previewLayer = [AVCaptureVideoPreviewLayer layerWithSession:self.captureSession];
    if (self.previewLayer) {
        self.previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
        self.previewLayer.connection.videoOrientation = AVCaptureVideoOrientationPortrait;
    }
}

- (void)updatePreviewLayerParentView:(UIView *)view {
    self.parentView = view;
    [self embedPreviewLayer];
}

- (void)embedPreviewLayer {
    if (!self.previewLayer) {
        [self setupLivePreview];
    }
    
    if (self.parentView) {
        [self updatePreviewLayerLayout];
        [self.parentView.layer addSublayer:self.previewLayer];
    } else {
        NSLog(@"Parent view for previewLayer is not configured");
    }
}

@end
