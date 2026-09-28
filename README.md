# Totem of Undying Broadcast

Client-side Fabric mod that broadcasts warnings about your Totems of Undying in chat.

## Commands

```text
/num-tou settings
/num-tou settings gui
/num-tou settings lang get
/num-tou settings lang set <en_us|zh_cn>
/num-tou settings warning-threshold get
/num-tou settings warning-threshold set <count>
/num-tou settings enable get
/num-tou settings enable set <true|false>
/num-tou settings check
/num-tou settings check tick get
/num-tou settings check tick set <ticks>
/num-tou settings check enable get
/num-tou settings check enable set <true|false>
/num-tou check
```

`/num-tou settings gui` opens the in-game configuration screen. Changes are
written only after selecting **Done**; **Cancel** closes the screen without saving.
When Mod Menu is installed, the same screen is available from this mod's
configuration button in the Mods list.

`settings enable` controls the existing count-change broadcasts: a warning is sent
when the count is first found below the threshold or decreases while below it.

`settings check enable` independently enables scheduled checks of both the total
totem count and whether the offhand contains a totem. Scheduled checks are disabled
by default. Each scheduled check broadcasts a warning for each failed condition,
even if the condition has not changed since the last check.

The default interval is 100 client ticks (about 5 seconds at normal tick speed).
`settings check tick set` accepts positive integers, starting at 1. Enabling checks,
changing the interval, or reconnecting starts a fresh interval. Checks pause while
the game is paused. `/num-tou check` runs an immediate check regardless of either
enable setting.

For example, to check every 10 seconds:

```text
/num-tou settings check tick set 200
/num-tou settings check enable set true
```

Settings are saved to `config/totem-of-undying-broadcast.json`. Older configuration
files use the default interval and leave scheduled checks disabled until enabled.

## Setup

For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) related to the IDE that you are using.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
