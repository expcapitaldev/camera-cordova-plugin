
#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>

@interface CCCameraOptions: NSObject

@property (copy, nonatomic) NSString  *title;
@property (copy, nonatomic) NSString  *subtitle;
@property (copy, nonatomic) NSString  *cancelText;
@property (copy, nonatomic) NSString  *message;
@property (copy, nonatomic) NSString  *viewFinderType;
@property (copy, nonatomic) NSString  *processingText;
@property (copy, nonatomic) NSString  *takingText;

- (CGFloat)aspectRatio;
- (BOOL)isViewFinderRequired;
- (BOOL)shouldUseFrontCamera;

@end
