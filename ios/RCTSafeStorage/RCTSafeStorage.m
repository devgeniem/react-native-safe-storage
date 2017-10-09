#import "RCTSafeStorage.h"
#import <Security/Security.h>
#import "RCTConvert.h"
#import "RCTBridge.h"
#import "RCTUtils.h"

@implementation SafeStorage

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(setEntry:(NSString*)key withData:(NSString*) value){
    NSString* service = [[NSBundle mainBundle] bundleIdentifier];
    
    // Create dictionary of search parameters
    NSDictionary* query = [NSDictionary dictionaryWithObjectsAndKeys:(__bridge id)(kSecClassGenericPassword),  kSecClass, service, kSecAttrService,  key, kSecAttrAccount, kCFBooleanTrue, kSecReturnAttributes, nil];
    
    // Remove any old values from the keychain
    OSStatus osStatus = SecItemDelete((__bridge CFDictionaryRef) query);
    
    // Create dictionary of parameters to add
    NSData* valueData = [value dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary* dict = [NSDictionary dictionaryWithObjectsAndKeys:(__bridge id)(kSecClassGenericPassword), kSecClass, service, kSecAttrService, valueData, kSecValueData, key, kSecAttrAccount, nil];
    
    osStatus = SecItemAdd((__bridge CFDictionaryRef) dict, NULL);
    
    if (osStatus != noErr && osStatus != errSecItemNotFound) {
        NSError *error = [NSError errorWithDomain:NSOSStatusErrorDomain code:osStatus userInfo:nil];
        NSLog(@"Error - could not save data to safe storeage: %@", error);
    }
}

RCT_EXPORT_METHOD(getEntry:(NSString*)key defaultValue:(NSString*) defaultValue callback:(RCTResponseSenderBlock)callback){
    NSString* service = [[NSBundle mainBundle] bundleIdentifier];
    
    // Create query dictionary
    NSDictionary* query = [NSDictionary dictionaryWithObjectsAndKeys:(__bridge id)(kSecClassGenericPassword), kSecClass, service, kSecAttrService, key, kSecAttrAccount, kCFBooleanTrue, kSecReturnAttributes, kCFBooleanTrue, kSecReturnData, nil];
    
    NSDictionary* found = nil;
    CFTypeRef foundTypeRef = NULL;
    OSStatus osStatus = SecItemCopyMatching((__bridge CFDictionaryRef) query, (CFTypeRef*)&foundTypeRef);
    
    if (osStatus != noErr && osStatus != errSecItemNotFound) {
        NSError *error = [NSError errorWithDomain:NSOSStatusErrorDomain code:osStatus userInfo:nil];
        NSLog(@"Error - could not get data from safe storeage: %@", error);
        return callback(@[defaultValue]);
    }
    
    found = (__bridge NSDictionary*)(foundTypeRef);
    if (!found) {
        return callback(@[defaultValue]);
    }
    
    // Return stored value
    NSString* value = [[NSString alloc] initWithData:[found objectForKey:(__bridge id)(kSecValueData)] encoding:NSUTF8StringEncoding];
    if(value){
        callback(@[value]);
    } else {
        callback(@[defaultValue]);
    }
}
@end
