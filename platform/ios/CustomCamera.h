
#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>

typedef NS_ENUM(NSUInteger, CCParamsIndex) {
    CCParamsIndexTitle = 0,
    CCParamsIndexSubtitle,
    CCParamsIndexCancelText,
    CCParamsIndexMessage,
    CCParamsIndexViewFinderType,
    CCParamsIndexProcessingText,
    CCParamsIndexTakingText
};

#pragma mark - CustomCamera

@interface CustomCamera: CDVPlugin {}

- (void)takePicture:(CDVInvokedUrlCommand *)command;

@end
