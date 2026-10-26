# Sun Panels

A simple Minecraft mod that adds three tiers of solar panels for generating Redstone Flux (RF) energy.

## Features

This mod adds three tiers of solar panels to Minecraft:

### Basic Solar Panel

- ⚡ Power Output: 32 RF/t
- 🔋 Internal Storage: 4,000 RF
- Affordable to craft, good for early game

### Advanced Solar Panel

- ⚡ Power Output: 128 RF/t
- 🔋 Internal Storage: 16,000 RF
- Requires mid-game resources, suitable for automation

### Elite Solar Panel

- ⚡ Power Output: 512 RF/t
- 🔋 Internal Storage: 64,000 RF
- Expensive to craft, but efficient for late-game setups

## How It Works

- Panels generate energy in daylight; output ramps smoothly at dawn, dusk and in rain and stops at night
- Panels need sky access, but transparent blocks such as glass on top are fine
- Energy is pushed automatically into adjacent energy-accepting blocks and pipes
- Right-click a panel to see its stored energy, current output and whether it sees the sky

## Energy Pipes

- Basic (100 RF/t), Advanced (500 RF/t), Elite (2,000 RF/t)
- Connected pipes form one network that carries power over any distance, limited by its slowest pipe
- Pipes never tick and store nothing: energy goes straight from the source to every machine on the network

## Recipes

### Basic Solar Panel
- 3x Glass
- 2x Redstone
- 1x Daylight Detector
- 3x Iron Ingots

### Advanced Solar Panel
- 3x Glass Panes
- 2x Redstone Blocks
- 1x Basic Solar Panel
- 3x Diamonds

### Elite Solar Panel
- 3x Glass
- 2x Redstone Blocks
- 1x Advanced Solar Panel
- 3x Netherite Ingots

### Energy Pipes
- Basic: 2x Iron Ingot + 1x Redstone in a row → 6 pipes
- Advanced: 8x Basic Pipe around 1x Gold Ingot → 8 pipes
- Elite: 8x Advanced Pipe around 1x Diamond → 8 pipes

## Multi-Mod Compatibility

This mod is designed to work with all major energy-using mods:

### RF/FE Based Mods (Native Compatibility)
Directly compatible with all Forge Energy (FE) based mods through the NeoForge energy system:

- ✅ **Thermal Series** (formerly Thermal Expansion)
- ✅ **RFTools Power**
- ✅ **Powah**
- ✅ **Ender IO**
- ✅ **Applied Energistics 2**
- ✅ **Immersive Engineering**

### Joules Based Mods
- ✅ **Mekanism**: Full integration with Mekanism's energy system
    - Energy is converted at the standard rate of 1 RF = 2.5 Joules
    - Solar panels automatically register with Mekanism via IMC

### EU Based Mods
- ✅ **Tech Reborn**: Compatible with Tech Reborn's EU system
    - Energy is converted at the standard rate of 4 RF = 1 EU
    - Fully supports Tech Reborn machines and cables

All compatibility is completely optional - the mod works fine without any of these mods installed, and will automatically detect and integrate with any that are present.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1 or higher

## License

This project is licensed under the MIT License - see the LICENSE file for details.

❓ Issues & Feedback

If you encounter any bugs or have suggestions, please leave a comment on this page! Your feedback helps improve the mod.

Enjoy Sun Panels and power up your world with clean, renewable energy! 🌞⚡
