# Warner Media Privacy Test App

This test app is used in order to test the functionality of the Warner Media privacy SDK.

## Information

This test app is written in Java and Kotlin and includes a DNS toggle button that calls functions from the Prism SDK in order to fulfill DNS requests.

## Installation

For more information on how the privacy SDK was implemented and used go to : https://warnerbros.sharepoint.com/sites/CCPA/Wiki/PrivacySDK.aspx

## Usage

The following methods are called and functions used in order to process DNS toggle requests.


### In MainActivity, Nexus is initialized as soon as possible

	initializeNexus(this.application)

### In PrivacySettingsActivity

#### Firstly, before the onCreate() method, the Privacy SDK is initialized: 

	val wmPrivacySdkInstance = WmPrivacySdk(mapOf())

  
#### Next, inside of the onCreate method, a call is made to the initPrism function :
  
  	wmPrivacySdkInstance.initPrism("BrandID","prod",this)


#### optOut() is called when DNS is enabled
Makes a call to the 

	wmPrivacySdkInstance.ccpaDoNotShare(this)

#### optIn() is called when DNS is disabled

	wmPrivacySdkInstance.ccpaShareData(this)

## Errors

If facing an error "symbol cannot be resolved" when importing nexus

	import com.turner.nexus.initializeNexus
Try using a different, static import instead



	import static com.turner.nexus.Android_native_apiKt.initializeNexus;
