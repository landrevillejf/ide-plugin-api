Feature: plugin event bus

  Background:
    * def Support = Java.type('com.protonmail.landrevillejf.ide.plugin.karate.KarateTestSupport')
    * def PluginEventBus = Java.type('com.protonmail.landrevillejf.ide.plugin.PluginEventBus')
    * def PluginLoadedEvent = Java.type('com.protonmail.landrevillejf.ide.plugin.events.PluginLoadedEvent')
    * def PluginUnloadedEvent = Java.type('com.protonmail.landrevillejf.ide.plugin.events.PluginUnloadedEvent')
    * Support.clear()

  Scenario: subscriber receives a published event
    * def bus = new PluginEventBus()
    * bus.subscribe(PluginLoadedEvent.class, Support.recordingListener())
    * assert bus.hasSubscribers(PluginLoadedEvent.class)
    * def event = new PluginLoadedEvent('karate', 'id-1', 'Karate Plugin', '1.0.0')
    * bus.publish(event)
    * match Support.receivedCount() == 1
    * match Support.lastEventType() == 'PluginLoadedEvent'
    * match Support.lastEvent().getPluginId() == 'id-1'
    * match Support.lastEvent().getSource() == 'karate'

  Scenario: unsubscribed listener stops receiving events
    * def bus = new PluginEventBus()
    * def listener = Support.recordingListener()
    * bus.subscribe(PluginLoadedEvent.class, listener)
    * bus.publish(new PluginLoadedEvent('karate', 'id-2', 'P2', '1.0.0'))
    * match Support.receivedCount() == 1
    * bus.unsubscribe(PluginLoadedEvent.class, listener)
    * bus.publish(new PluginLoadedEvent('karate', 'id-2', 'P2', '1.0.0'))
    * match Support.receivedCount() == 1

  Scenario: publishing with no subscribers is a no-op
    * def bus = new PluginEventBus()
    * assert !bus.hasSubscribers(PluginLoadedEvent.class)
    * bus.publish(new PluginLoadedEvent('karate', 'id-3', 'P3', '1.0.0'))
    * match Support.receivedCount() == 0

  Scenario: event types are dispatched independently
    * def bus = new PluginEventBus()
    * bus.subscribe(PluginLoadedEvent.class, Support.recordingListener())
    * bus.publish(new PluginUnloadedEvent('karate', 'other', 'OtherPlugin'))
    * match Support.receivedCount() == 0
    * bus.publish(new PluginLoadedEvent('karate', 'id-4', 'P4', '2.0.0'))
    * match Support.receivedCount() == 1

  Scenario: clearing a single event type keeps others
    * def bus = new PluginEventBus()
    * bus.subscribe(PluginLoadedEvent.class, Support.recordingListener())
    * bus.subscribe(PluginUnloadedEvent.class, Support.recordingListener())
    * bus.clear(PluginLoadedEvent.class)
    * assert !bus.hasSubscribers(PluginLoadedEvent.class)
    * assert bus.hasSubscribers(PluginUnloadedEvent.class)

  Scenario: clearing the bus removes all subscriptions
    * def bus = new PluginEventBus()
    * bus.subscribe(PluginLoadedEvent.class, Support.recordingListener())
    * bus.subscribe(PluginUnloadedEvent.class, Support.recordingListener())
    * bus.clear()
    * assert !bus.hasSubscribers(PluginLoadedEvent.class)
    * assert !bus.hasSubscribers(PluginUnloadedEvent.class)
