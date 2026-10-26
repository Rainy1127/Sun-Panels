# Sun Panels (NeoForge 26.1)

Simple solar panels and lag-free energy pipes for Minecraft 26.1.2 / NeoForge, Java 25.

## Solar panels

| Tier | Output in clear daylight | Buffer |
|---|---|---|
| Basic | 32 RF/t | 4,000 RF |
| Advanced | 128 RF/t | 16,000 RF |
| Elite | 512 RF/t | 64,000 RF |

- Need sky access above them; glass and other transparent blocks on top are fine.
- Output follows the effective sky brightness: full in clear daylight, a smooth ramp at dawn, dusk and in rain, nothing at night.
- Push their energy into every adjacent block or pipe that accepts it. Other blocks can pull from them, nothing can push into them.
- Right-click opens a dark status screen: stored energy, current output, and whether the panel sees the sky.

## Energy pipes

| Tier | Network throughput |
|---|---|
| Basic | 100 RF/t |
| Advanced | 500 RF/t |
| Elite | 2,000 RF/t |

- Connected pipes form one network. Energy put into any pipe goes straight to every machine on the network, over any distance.
- A network moves at most the rate of its slowest pipe per tick.
- Pipes never tick and store nothing. The layout is cached and only rebuilt when a pipe is placed, broken, loaded or unloaded.
- Built on NeoForge's transactional energy API (`EnergyHandler`), so simulated and cancelled transfers are always exact.

## Building

```
./gradlew build
```
The jar is written to `build/libs/`.

## License

MIT, see `LICENSE`.
