//
//  ViewController.m
//  MQTT iOS
//
//  Created by Ferdinand Silva on 08/08/2018.
//  Copyright © 2018 Ferdinand Silva. All rights reserved.
//

#import "ViewController.h"

@interface ViewController ()
@property (weak, nonatomic) IBOutlet UIButton *buttonSwitch;
@property (strong, nonatomic) MQTTSessionManager *sessionManager;
@end

@implementation ViewController
NSString * const SERVER_URI = @"test.mosquitto.org";
const int SERVER_PORT = 1883;
NSString * const TOPIC_SWITCH = @"led_switch";
NSString * const TOPIC_STATUS = @"led_status";
NSString * const TOPIC_GET_STATUS = @"led_get_status";
bool isLedOn = false;

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
    if(!self.sessionManager) {
        self.sessionManager = [[MQTTSessionManager alloc] init];
        self.sessionManager.delegate = self;
        
        self.sessionManager.subscriptions = [NSDictionary dictionaryWithObject:[NSNumber numberWithInt:MQTTQosLevelExactlyOnce] forKey:TOPIC_STATUS];
        
        [self.sessionManager connectTo:SERVER_URI
                                  port: SERVER_PORT
                                   tls:false
                             keepalive:60
                                 clean:true
                                  auth:false
                                  user:nil
                                  pass:nil
                                  will:false
                             willTopic:nil
                               willMsg:nil
                               willQos:MQTTQosLevelExactlyOnce
                        willRetainFlag:false
                          withClientId:nil
                        securityPolicy:nil
                          certificates:nil
                         protocolLevel:MQTTProtocolVersion31
                        connectHandler: ^(NSError *error) {
                            NSLog(@"%@", error);
                        }];
        
    } else {
        [self.sessionManager connectToLast: ^(NSError *error) {
            NSLog(@"%@", error);
        }];
    }
    
    [self.sessionManager addObserver:self
                          forKeyPath:@"state"
                             options:NSKeyValueObservingOptionInitial | NSKeyValueObservingOptionNew
                             context:nil];
}

- (void)publishMessage: (NSString *)msg : (NSString *)topic {
    [self.sessionManager sendData:[msg dataUsingEncoding:NSUTF8StringEncoding]
                            topic:topic
                              qos:MQTTQosLevelExactlyOnce
                           retain:false];
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context {
    if (self.sessionManager.state == MQTTSessionManagerStateConnected) {
        [self publishMessage:@"GET_STAT" :TOPIC_GET_STATUS];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
- (IBAction)buttonClicked:(id)sender {
    
    if (self.sessionManager.state == MQTTSessionManagerStateConnected) {
        NSString *msg;
        if(isLedOn) {
            msg = @"0";
        } else {
            msg = @"1";
        }
        
        [self publishMessage:msg :TOPIC_SWITCH];
    }
}

- (void)handleMessage:(NSData *)data onTopic:(NSString *)topic retained:(BOOL)retained {
    NSString *dataString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];

    if([dataString isEqualToString:@"0"]) {
        isLedOn = false;
        [_buttonSwitch setTitle:@"TURN LED ON" forState:UIControlStateNormal];
    } else {
        isLedOn = true;
        [_buttonSwitch setTitle:@"TURN LED OFF" forState:UIControlStateNormal];
    }
}

@end
