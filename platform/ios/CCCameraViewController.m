
#import "CCCameraViewController.h"
#import "CCCameraManager.h"

@interface CCCameraViewController ()

@property (weak, nonatomic) IBOutlet UIView *cameraView;
@property (weak, nonatomic) IBOutlet UIView *cameraMaskView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *cameraMaskWidthConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *cameraMaskHeightConstraint;

@property (weak, nonatomic) IBOutlet UIButton *captureButton;
@property (weak, nonatomic) IBOutlet UIButton *cancelButton;
@property (weak, nonatomic) IBOutlet UILabel *titleLabel;
@property (weak, nonatomic) IBOutlet UILabel *subtitleLabel;
@property (weak, nonatomic) IBOutlet UILabel *messageLabel;

@property (assign, nonatomic) BOOL isInited;
@property (strong, nonatomic) CCCameraManager *cameraManager;
@property (strong, nonatomic) UIImage *photo;

@property (strong, nonatomic) CAShapeLayer *shadeLayer;

@end

@implementation CCCameraViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self setupUI];
    
    CCCameraType cameraType = [self.options shouldUseFrontCamera] ? CCCameraTypeFront : CCCameraTypeBack;
    self.cameraManager = [[CCCameraManager alloc] initWithCamera:cameraType];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [self.cameraManager addPreviewLayerToView:self.cameraView];
    [self.cameraManager startSession];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [self.cameraManager stopSession];
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    
    if (!self.isInited) {
        self.isInited = YES;
        [self.cameraManager updatePreviewLayerLayout];
        [self prepareCameraMask];
    }
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    if (self.presentingViewController) {
        return [self.presentingViewController supportedInterfaceOrientations];
    }
    return UIInterfaceOrientationMaskPortrait | UIInterfaceOrientationMaskPortraitUpsideDown;
}

- (void)setOptions:(CCCameraOptions *)options {
    _options = options;
    [self updateUI];
}

// MARK: - Actions

- (IBAction)tapCameraButton:(id)sender {
    [self.captureButton setEnabled:NO];
    [self runFlashAnimation];
    
    __weak CCCameraViewController *weakSelf = self;
    [self.cameraManager capturePhoto: ^(UIImage *image, NSError *error) {
        if (image) {
            [weakSelf didMakePhoto:image];
        } else if (error) {
            [weakSelf restartPhoto];
            NSLog(@"Failed to get photo: %@", error);
        }
    }];
}

- (IBAction)tapCancelButton:(id)sender {
    [self.delegate cameraControllerDidCancel:self];
}

// MARK: - Private methods

- (void)setupUI {
    self.view.backgroundColor = [UIColor blackColor];
    [self.captureButton setImage:[UIImage imageNamed:@"cc_capture_btn"] forState:UIControlStateNormal];

    [self updateUI];
}

- (void)updateUI {
    [self setupLabel:self.titleLabel withText:self.options.title];
    [self setupLabel:self.messageLabel withText:self.options.message];
    [self setupButton:self.cancelButton withTitle:self.options.cancelText];
    
    [self setupLabel:self.subtitleLabel withText:self.options.subtitle];
    self.subtitleLabel.font =[UIFont systemFontOfSize:14 weight:UIFontWeightMedium];
    [self.subtitleLabel setHidden:!self.subtitleLabel.text];
}

- (void)setupButton: (UIButton *)button withTitle:(NSString *)title {
    [button setTitle:title forState:UIControlStateNormal];
    button.titleLabel.font = [UIFont systemFontOfSize:16];
    button.titleLabel.textColor = [UIColor whiteColor];
}

- (void)setupLabel: (UILabel *)label withText:(NSString *)text {
    label.backgroundColor = [UIColor clearColor];
    label.font = [UIFont systemFontOfSize:16 weight:UIFontWeightMedium];
    label.textColor = [UIColor whiteColor];
    label.text = text;
}

- (void)runFlashAnimation {
    [self.cameraView.layer removeAllAnimations];
    
    self.cameraView.alpha = 0;
    __weak CCCameraViewController *weakSelf = self;
    [UIView animateWithDuration:0.25
                     animations:^{
        weakSelf.cameraView.alpha = 1.0;
    }];
}

- (void)didMakePhoto:(UIImage *)image {
    self.photo = image;
    [self.delegate cameraControllerDidUsePhoto:self.photo];
}

- (void)restartPhoto {
    [self.captureButton setEnabled:YES];
    self.photo = nil;
}

- (void)prepareCameraMask {
    if (![self.options isViewFinderRequired]) {
        [self.cameraMaskView setHidden:YES];
    } else {
        [self.cameraMaskView setHidden:NO];
        [self updateCameraMaskSize];

        UIBezierPath *overlayPath = [UIBezierPath bezierPathWithRect:self.cameraView.bounds];
        CGRect maskRect = [self.cameraView convertRect:self.cameraMaskView.frame fromView:self.view];
        UIBezierPath *cameraMaskPath = [UIBezierPath bezierPathWithRoundedRect:maskRect cornerRadius:4.0];
        [overlayPath appendPath:cameraMaskPath];
        overlayPath.usesEvenOddFillRule = YES;
        
        CAShapeLayer *cameraOverlayLayer = [CAShapeLayer new];
        [self.shadeLayer removeFromSuperlayer];
        self.shadeLayer = cameraOverlayLayer;
        self.shadeLayer.path = overlayPath.CGPath;
        self.shadeLayer.fillRule = kCAFillRuleEvenOdd;
        self.shadeLayer.fillColor = [UIColor blackColor].CGColor;
        self.shadeLayer.opacity = 0.3;
        [self.cameraView.layer addSublayer:self.shadeLayer];
        
        self.cameraMaskView.layer.borderWidth = 2.0;
        self.cameraMaskView.layer.borderColor = [UIColor whiteColor].CGColor;
        self.cameraMaskView.layer.cornerRadius = 4.0;
        self.cameraMaskView.layer.masksToBounds = YES;
    }
}

- (void)updateCameraMaskSize {
    CGFloat offset = 16.0;
    CGSize cameraSize = self.cameraView.bounds.size;
    CGFloat viewFinderAspectRatio = self.options.aspectRatio;
    
    if (cameraSize.height / cameraSize.width > viewFinderAspectRatio) {
        self.cameraMaskWidthConstraint.constant = cameraSize.width - 2.0 * offset;
        self.cameraMaskHeightConstraint.constant = self.cameraMaskHeightConstraint.constant * viewFinderAspectRatio;
    } else {
        self.cameraMaskHeightConstraint.constant = cameraSize.height - 2.0 * offset;
        self.cameraMaskWidthConstraint.constant = self.cameraMaskHeightConstraint.constant / viewFinderAspectRatio;
    }
    
    [self.view layoutIfNeeded];
}

@end
