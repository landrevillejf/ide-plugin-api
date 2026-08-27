Feature: plugin lifecycle state machine and descriptor

  Background:
    * def PluginStatus = Java.type('com.protonmail.landrevillejf.ide.plugin.PluginStatus')
    * def PluginDescriptor = Java.type('com.protonmail.landrevillejf.ide.plugin.PluginDescriptor')

  Scenario: happy-path state transitions are allowed
    * assert PluginStatus.UNLOADED.canTransitionTo(PluginStatus.LOADED)
    * assert PluginStatus.LOADED.canTransitionTo(PluginStatus.INITIALIZED)
    * assert PluginStatus.INITIALIZED.canTransitionTo(PluginStatus.ENABLED)
    * assert PluginStatus.ENABLED.canTransitionTo(PluginStatus.DISABLED)
    * assert PluginStatus.DISABLED.canTransitionTo(PluginStatus.SHUTTING_DOWN)
    * assert PluginStatus.SHUTTING_DOWN.canTransitionTo(PluginStatus.SHUTDOWN)

  Scenario: invalid transitions are rejected
    * assert !PluginStatus.UNLOADED.canTransitionTo(PluginStatus.ENABLED)
    * assert !PluginStatus.SHUTDOWN.canTransitionTo(PluginStatus.LOADED)
    * assert !PluginStatus.UNLOADED.canTransitionTo(null)

  Scenario: same-state transitions are idempotent
    * assert PluginStatus.ENABLED.canTransitionTo(PluginStatus.ENABLED)
    * assert PluginStatus.DISABLED.canTransitionTo(PluginStatus.DISABLED)

  Scenario: only ENABLED is active
    * assert PluginStatus.ENABLED.isActive()
    * assert !PluginStatus.DISABLED.isActive()
    * assert !PluginStatus.LOADED.isActive()
    * assert PluginStatus.DISABLED.isInactive()
    * assert !PluginStatus.ENABLED.isInactive()

  Scenario: error state and recovery path
    * assert PluginStatus.ENABLED.canTransitionTo(PluginStatus.ERROR)
    * assert PluginStatus.INITIALIZED.canTransitionTo(PluginStatus.ERROR)
    * assert PluginStatus.ERROR.canTransitionTo(PluginStatus.DISABLED)
    * assert !PluginStatus.ERROR.canTransitionTo(PluginStatus.ENABLED)
    * assert !PluginStatus.UNLOADED.canTransitionTo(PluginStatus.ERROR)

  Scenario: descriptor metadata round-trip
    * def descriptor = new PluginDescriptor()
    * descriptor.setId('karate-plugin')
    * descriptor.setName('Karate Plugin')
    * descriptor.setVersion('2.0.0')
    * descriptor.setMainClass('com.example.KaratePlugin')
    * descriptor.setAuthor('Karate')
    * match descriptor.getId() == 'karate-plugin'
    * match descriptor.getName() == 'Karate Plugin'
    * match descriptor.getVersion() == '2.0.0'
    * match descriptor.getMainClass() == 'com.example.KaratePlugin'
    * match descriptor.getAuthor() == 'Karate'
    * match descriptor.getCategory() == 'General'

  Scenario: descriptor copy produces a distinct instance with same identity fields
    * def original = new PluginDescriptor()
    * original.setId('copy-test')
    * original.setName('Copy Test')
    * original.setVersion('1.2.3')
    * def copied = original.copy()
    * match copied.getId() == 'copy-test'
    * match copied.getName() == 'Copy Test'
    * match copied.getVersion() == '1.2.3'
    * assert copied != original
