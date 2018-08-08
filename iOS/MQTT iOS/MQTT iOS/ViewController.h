//
//  ViewController.h
//  MQTT iOS
//
//  Created by Ferdinand Silva on 08/08/2018.
//  Copyright © 2018 Ferdinand Silva. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MQTTClient.h"

@interface ViewController : UIViewController <MQTTSessionManagerDelegate>
- (void)publishMessage: (NSString *)msg : (NSString *)topic;
@end

