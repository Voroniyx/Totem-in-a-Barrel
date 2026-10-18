# The Barrel Totem Mod
With this mod, you can place a single totem **with your name** in a barrel and consume it when you activate a totem.

## How it works
If a players totem pops and simultaneously placed a totem with their name in a barrel, and the chunk containing the barrel is loaded, the totem in the barrel is consumed, emptying the barrel and stopping the redstone signal emitted by the barrel. In combination with a stasis chamber, this can trigger a trapdoor, which then triggers the trapdoor and transports the player into the stasis chamber.
An example of such a configuration can be found in the gallery.

## Configuration

### Config Command
*Only available in version 1.3.0+*

You can manage all settings via the new command `/tiab <subcommand> <...options>`

#### Sub Commands
- `... config <Get|EnableTotemConsume|TotemConsumeOnlyWhenLastTotemUsed> [true|false]`
- `... override <player> <EnableTotemConsume|TotemConsumeOnlyWhenLastTotemUsed|NameOfTriggeringTotem> <true|false|greedy_string>`

Default config:
```json
{
  "EnableTotemConsume": true,
  "TotemConsumeOnlyWhenLastTotemUsed": true
}
```
| Variable | Default value | Available in | Description |
|---|---|---|---|
| EnableTotemConsume | true | Global, Override | When `true` enables the mod |
| TotemConsumeOnlyWhenLastTotemUsed | true | Global, Override | When `true`, the totem in the barrel will only be consumed when the players last totem poped |
| NameOfTriggeringTotem | *unset* | Override | When set, the barrel totem is only consumed if the totem that popped is named this. Matching ignores casing and surrounding spaces. Unset means any totem counts |

## Experimental: Marked Barrels
Mimics the original glitch without needing a second totem. This can only be enabled in the config file and requires a server restart.

```json
{
  "EnableExperimentalMarkedBarrels": true,
  "ExperimentalMarkerBlock": "minecraft:crying_obsidian"
}
```

- A barrel is **marked** when the block directly below it is the marker block (`ExperimentalMarkerBlock`).
- When a player puts a totem into a marked barrel (click, shift-click or number key), the action is cancelled: the player keeps their totem and the barrel receives a totem named after the player instead. The normal totem pop logic then consumes it.
- Taking a totem out of a marked barrel makes it vanish.
- The generated totems are tagged, so they always vanish when they leave a barrel (menu, hopper, broken barrel). This prevents duplicating totems.

## Unstable SMP / Origin
I made this mod because the original glitch/bug, whatever you want to call it, got fixed in 1.18 (to my knowledge). It used update suppression to create a ghost item in a barrel/chest. That block still emitted a redstone signal, and when the player who held another totem popped that totem, the ghost item would disappear, the emitted redstone signal would stop, and whatever mechanism they'd put behind that got triggered. The same thing is possible to do with this mod, just without the big redstone machine in the background that was required to create the update suppression.

## AI Disclaimer
I used AI to update the Modrinth description, specifically the Markdown table, and also to check my text for spelling and grammar. I also used AI to first convert the fabric mod into a paper plugin, since at that point I didn’t yet know how to set up a multiloader project.
I also use AI to identify performance gaps. BUT I made ALL the changes by hand after the initial convert and still DO NOT use AI to write code for me.
