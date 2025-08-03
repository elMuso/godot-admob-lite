# Why this instead of the other godot admob plugin?

Because the other should be called godot ADS plugin, its too bloated, very confusing and since it uses the full library of admob it adds 40mb to your android app.

This one adds less than 2 mb, but it also is more limited. Only banner (with 3 positions), Interstitial and Rewarded ads are supported.

But since its so simple it only has 3 kotlin classes, so you can extend it to fit your needs.

# Installing the plugin

1. Copy the folder GodotAdmobLite to your addons project folder (create the folder if there is none)
2. Navigate to Project -> Project Settings... -> Plugins, and ensure the plugin is enabled 
3. Install the Godot Android build template by clicking on Project -> Install Android Build Template...
4. Navigate to Project -> Export...
5. In the Export window, create an Android export preset
6. In the Android export preset, scroll to Gradle Build and set Use Gradle Build to true

# One time setup after installing the plugin (IMPORTANT!!!!)

You need an admob app id, google provides a sample one, either way, modify your android export preset's androidmanifest.xml to include this.
If you don't do this your app WILL crash (blame google not me), as the appid is both used for admob and google's UMP

```
<manifest>
<application>
<!-- Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713 -->
<meta-data
android:name="com.google.android.gms.ads.APPLICATION_ID"
android:value="ca-app-pub-3940256099942544~3347511713"/> <!-- MODIFY THIS!! -->
</application>
</manifest>
```

# How to use the plugin

You interface with it with the GodotAdmobLite, using this interface you pass the AD_ID as a main argument, there are extra arguments to pass as well

Using this plugin showing a banner is just 3 extra lines of code

The initialize() method already takes care of checking for a form on ump, showing it if possible (or needed) and then triggering the complete signal

```gd
extends Node2D
var plugin 
func _ready() -> void:
	GodotAdmobLite.on_initialization_complete.connect(_on_initialization_result)
	GodotAdmobLite.initialize()

func _on_initialization_result(success:bool):
	GodotAdmobLite.load_and_show_banner("your_admob_banner_id_here",GodotAdmobLite.BannerSize.FULL_BANNER,GodotAdmobLite.BannerPosition.BOTTOM)

```

IF YOU ARE NOT ON RELEASE MODE YOUR ADS WILL SHOW AS DEBUG MODE, this is enforced since google kinda cares about false impressions.

Also you can interface with google's UMP by calling the methods that start with GodotAdmobLite.ump.... to show the form again or the privacy popup, or enabling debug information

Use godot autocomplete to check for the available methods, or go to interface.gd then



If you edit the source code and want some help here it is

Used android studio narwhal 2025.1.1
JAVA 17


run ./gradlew assemble, the folder is generated on plugin/demo/addons/GodotAdmobLite , copy the folder to your addons folder