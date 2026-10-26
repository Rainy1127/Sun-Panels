# Changelog

## 1.1.0 (Minecraft 1.21.1)
- New: right-click a panel to open a dark status screen with stored energy, current output and sky access. It works for all three tiers.
- New textures for panels and pipes: the tier is shown by colour (cyan, orange, red) and by the dots on the panel side.
- New mod logo.
- Russian translation.
- Fixed: a glass block directly on top of a panel stopped generation. Panels now check sky light, which passes through glass.
- Fixed: power did not travel through more than one pipe. Pipes are now a tick-less network that carries power over any length.
- Fixed: panels produced only half of their advertised output during the day. Output now reaches the full rated value in clear daylight.
- Fixed: crafting recipes did not load, and panels and pipes dropped nothing when mined.
- Added: crafting recipes for the three pipe tiers.
- Removed the panel log message that was written every 10 seconds.
