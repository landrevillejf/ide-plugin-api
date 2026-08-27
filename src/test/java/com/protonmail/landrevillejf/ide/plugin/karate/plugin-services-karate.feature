Feature: plugin configuration service

  Background:
    * def PluginConfig = Java.type('com.protonmail.landrevillejf.ide.plugin.PluginConfig')

  Scenario: settings round-trip with typed accessors
    * def config = new PluginConfig()
    * config.setSetting('editor.fontSize', 14)
    * config.setSetting('editor.theme', 'dark')
    * config.setSetting('editor.wrap', true)
    * match config.getSettingAsString('editor.theme', 'light') == 'dark'
    * match config.getSettingAsInt('editor.fontSize', 12) == 14
    * match config.getSettingAsBoolean('editor.wrap', false) == true
    * assert config.hasSetting('editor.theme')
    * assert !config.hasSetting('editor.unknown')

  Scenario: defaults are returned for missing settings
    * def config = new PluginConfig()
    * match config.getSettingAsString('missing', 'fallback') == 'fallback'
    * match config.getSettingAsInt('missing', 42) == 42
    * match config.getSettingAsBoolean('missing', true) == true

  Scenario: settings can be removed
    * def config = new PluginConfig()
    * config.setSetting('temp', 'value')
    * assert config.hasSetting('temp')
    * config.removeSetting('temp')
    * assert !config.hasSetting('temp')

  Scenario: feature flags can be enabled and disabled
    * def config = new PluginConfig()
    * config.enableFeature('auto-save')
    * assert config.isFeatureEnabled('auto-save')
    * assert config.getEnabledFeatures().contains('auto-save')
    * config.disableFeature('auto-save')
    * assert !config.isFeatureEnabled('auto-save')

  Scenario: auto-enable can be toggled
    * def config = new PluginConfig()
    * config.setAutoEnable(true)
    * assert config.isAutoEnable()
    * config.setAutoEnable(false)
    * assert !config.isAutoEnable()

  Scenario: configuration reset clears everything
    * def config = new PluginConfig()
    * config.setSetting('a', '1')
    * config.enableFeature('f')
    * config.setAutoEnable(true)
    * config.reset()
    * assert !config.hasSetting('a')
    * assert !config.isFeatureEnabled('f')
