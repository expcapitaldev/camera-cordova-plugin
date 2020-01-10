
#import "CCCameraOptions.h"

#pragma mark - Constants

static NSString *const kCCViewFinderTypeNone = @"NONE";
static NSString *const kCCViewFinderTypeLicence = @"LICENCE";
static NSString *const kCCViewFinderTypePassport = @"PASSPORT";
static NSString *const kCCViewFinderTypeFace = @"FACE";
static NSString *const kCCViewFinderTypeA4 = @"A4";

#pragma mark - CCCameraOptions Implementation

@implementation CCCameraOptions

+ (NSDictionary *)aspectRatioByViewFinderTypeMap {
    return  @{
        kCCViewFinderTypeNone       : @(1.0),
        kCCViewFinderTypeLicence    : @(0.62),
        kCCViewFinderTypePassport   : @(0.68),
        kCCViewFinderTypeFace       : @(1.41),
        kCCViewFinderTypeA4         : @(1.41),
    };
}

+ (CGFloat)aspectRatioForViewFinderType: (NSString *)type {
    NSNumber *ratioNumber = [self aspectRatioByViewFinderTypeMap][type];
    if (ratioNumber) {
        return ratioNumber.floatValue;
    }
    return 1.0;
}

// MARK: - Public methods

- (CGFloat)aspectRatio {
    return [CCCameraOptions aspectRatioForViewFinderType:self.viewFinderType];
}

- (BOOL)isViewFinderRequired {
    return self.viewFinderType != nil && ![self.viewFinderType isEqualToString:kCCViewFinderTypeNone];
}

- (BOOL)shouldUseFrontCamera {
    return self.viewFinderType != nil && [self.viewFinderType isEqualToString:kCCViewFinderTypeFace];
}

@end
