
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, CCCameraType) {
    CCCameraTypeBack,
    CCCameraTypeFront
};

typedef void (^CCPhotoCaptureBlock) (UIImage * _Nullable image, NSError * _Nullable error);

@interface CCCameraManager : NSObject

- (instancetype)initWithCamera:(CCCameraType)cameraType;

- (void)addPreviewLayerToView:(UIView *)view;
- (void)updatePreviewLayerLayout;

- (void)startSession;
- (void)stopSession;

- (void)capturePhoto:(CCPhotoCaptureBlock)completion;

@end

NS_ASSUME_NONNULL_END
