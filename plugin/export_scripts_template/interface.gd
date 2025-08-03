extends Node

var _plugin_name = "GodotAdmobLite"
var _android_plugin

enum InterstitialResult{
	FAILED,
	DISMISSED,
	SHOWED,
	IMPRESSION,
	CLICK
}
enum RewardResult{
	FAILED,
	DISMISSED,
	SHOWED,
	IMPRESSION,
	CLICK
}

enum GeoMode{
	DISABLED,
	EEA,
	REGULATED_EEUU,
	OTHER
}

enum BannerSize{
	BANNER,
	LARGE_BANNER,
	MEDIUM_RECTANGLE,
	FULL_BANNER,
	LEADERBOARD
}

enum BannerPosition{
	TOP,
	CENTER,
	BOTTOM
}

signal on_initialization_complete(activeAds:bool)
signal on_interstitial_loaded(id:String, success:bool)
signal on_interstitial_shown(id:String, result:InterstitialResult)
signal on_rewarded_loaded(id:String, success:bool)
signal on_reward_shown(id:String, result:RewardResult)
signal on_reward_received(id:String, amount:int, type:String)

func _ready():
	if Engine.has_singleton(_plugin_name):
		_android_plugin = Engine.get_singleton(_plugin_name)
		_android_plugin.on_initialization_complete.connect(
			func(active): on_initialization_complete.emit(active)
		)
		_android_plugin.on_interstitial_loaded.connect(
			func(id,success): on_interstitial_loaded.emit(id,success)
		)
		_android_plugin.on_interstitial_shown.connect(
			func(id,result): on_interstitial_shown.emit(id,result)
		)
		_android_plugin.on_rewarded_loaded.connect(
			func(id,success): on_rewarded_loaded.emit(id,success)
		)
		_android_plugin.on_reward_shown.connect(
			func(id,result): on_reward_shown.emit(id,result)
		)
		_android_plugin.on_reward_received.connect(
			func(id,amount,type): on_reward_shown.emit(id,amount,type)
		)
	else:
		printerr("Couldn't find plugin " + _plugin_name)

func ump_enable_debug_mode(device_id:String, mode:GeoMode):
	if _android_plugin:
		_android_plugin.enableDebugMode(device_id,mode)

func ump_show_form():
	if _android_plugin:
		_android_plugin.showUmpForm()

func ump_show_privacy_form():
	if _android_plugin:
		_android_plugin.showPrivacyForm()

func ump_debug_reset():
	if _android_plugin:
		_android_plugin.debugResetForm()

func initialize():
	if _android_plugin:
		_android_plugin.initialize()

#If size is bigger than 40 it will be treated as width
func load_and_show_banner_with_fixed_width(id:String, size:int, position:BannerPosition):
	load_and_show_banner(id,min(40,size),position)

func load_and_show_banner(id:String, size:BannerSize, position:BannerPosition):
	if _android_plugin:
		_android_plugin.loadAndShowBanner(id,size,position)

func destroy_banner(id:String):
	if _android_plugin:
		_android_plugin.destroyBanner(id)

func load_interstitial(id:String):
	if _android_plugin:
		_android_plugin.loadInterstitial(id)

func show_interstitial(id:String):
	if _android_plugin:
		_android_plugin.showInterstitial(id)

func release_interstitial(id:String):
	if _android_plugin:
		_android_plugin.releaseInterstitial(id)

func load_rewarded(id:String):
	if _android_plugin:
		_android_plugin.loadRewarded(id)

func show_rewarded(id:String):
	if _android_plugin:
		_android_plugin.showRewarded(id)

func release_rewarded(id:String):
	if _android_plugin:
		_android_plugin.releaseRewarded(id)
