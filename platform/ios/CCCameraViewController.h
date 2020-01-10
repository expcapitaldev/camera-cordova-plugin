
#import <UIKit/UIKit.h>
#import "CCCameraOptions.h"

@protocol CCCameraViewControllerDelegate;

NS_ASSUME_NONNULL_BEGIN

// MARK: - CCCameraViewController

@interface CCCameraViewController : UIViewController

@property (copy, nonatomic) NSString *callbackId;
@property (strong, nonatomic) UIView *webView;
@property (strong, nonatomic) CCCameraOptions *options;
@property (weak, nonatomic) id<CCCameraViewControllerDelegate> delegate;

@end

// MARK: - CCCameraViewControllerDelegate

@protocol CCCameraViewControllerDelegate <NSObject>

- (void)cameraControllerDidCancel:(CCCameraViewController *)controller;
- (void)cameraControllerDidUsePhoto:(UIImage *)photo;

@end

NS_ASSUME_NONNULL_END
