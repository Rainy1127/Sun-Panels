"""Generates the Sun Panels block textures (16x16, tech-minimalist dark style).

Usage: python3 tools/gen_textures.py <assets/sun_panels/textures/block directory>
Tier accents follow the mod palette: basic = neon cyan, advanced = neon orange, elite = neon red.
"""
import os
import sys

from PIL import Image

ACCENTS = {
    "basic": ((0, 229, 255), (0, 120, 140)),
    "advanced": ((255, 159, 28), (150, 84, 10)),
    "elite": ((255, 59, 92), (140, 24, 44)),
}

FRAME_HI = (70, 76, 92)
FRAME = (44, 48, 60)
FRAME_LO = (26, 28, 36)
EDGE = (12, 13, 18)
CASING = (32, 35, 44)
CASING_HI = (46, 50, 62)
CASING_LO = (22, 24, 31)
SEPARATOR = (6, 9, 16)


def mix(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def cell_color(x, y, tier):
    """Blue photovoltaic cell with a top-left to bottom-right gradient and a diagonal glare band."""
    light, dark = (34, 92, 160), (10, 34, 72)
    if tier == "elite":
        light, dark = (26, 60, 120), (6, 18, 44)
    c = mix(light, dark, (x + y) / 28.0)
    d = x - y
    if d in (3, 4):
        c = mix(c, (150, 200, 255), 0.22)
    elif d == 5:
        c = mix(c, (150, 200, 255), 0.10)
    return c


def panel_top(tier):
    accent, accent_dim = ACCENTS[tier]
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    separators = (5, 10)
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                c = FRAME_HI if (x == 0 or y == 0) else FRAME_LO
                if (x, y) in ((0, 0), (15, 0), (0, 15), (15, 15)):
                    c = EDGE
            elif x in separators or y in separators:
                c = SEPARATOR
                if tier == "elite":
                    c = mix(accent_dim, SEPARATOR, 0.35)
            else:
                c = cell_color(x, y, tier)
                if tier == "advanced" and x % 2 == 0:
                    c = mix(c, SEPARATOR, 0.35)
                if tier == "elite" and (x % 2 == 0 or y % 2 == 0):
                    c = mix(c, SEPARATOR, 0.30)
            px[x, y] = c + (255,)
    for cx, cy in ((1, 1), (14, 1), (1, 14), (14, 14)):
        px[cx, cy] = accent + (255,)
    if tier != "basic":
        for sx in separators:
            for sy in separators:
                px[sx, sy] = accent + (255,)
    return img


def panel_side(tier):
    accent, accent_dim = ACCENTS[tier]
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            if y == 0:
                c = FRAME_HI
            elif y == 1:
                c = cell_color(x, 2, tier)
            elif y == 2:
                c = FRAME_LO
            elif y == 15:
                c = EDGE
            elif x in (0, 15):
                c = CASING_HI if x == 0 else CASING_LO
            else:
                c = CASING
                if y in (3, 14):
                    c = CASING_HI if y == 3 else CASING_LO
            px[x, y] = c + (255,)
    for x in range(3, 13):
        px[x, 9] = accent + (255,)
        px[x, 10] = accent_dim + (255,)
    px[2, 9] = px[13, 9] = EDGE + (255,)
    px[2, 10] = px[13, 10] = EDGE + (255,)
    pips = {"basic": [7], "advanced": [6, 9], "elite": [4, 7, 10]}[tier]
    for x in pips:
        for dx in (0, 1):
            px[x + dx, 6] = accent + (255,)
    for x in range(2, 14, 3):
        px[x, 12] = CASING_LO + (255,)
    return img


def panel_bottom():
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                c = EDGE
            elif x in (1, 14) or y in (1, 14):
                c = CASING_HI if (x == 1 or y == 1) else CASING_LO
            else:
                c = CASING_LO if (x + y) % 4 == 0 else CASING
            px[x, y] = c + (255,)
    for sx, sy in ((3, 3), (12, 3), (3, 12), (12, 12)):
        px[sx, sy] = FRAME_HI + (255,)
    return img


def pipe(tier):
    """Arm sides use the horizontal band (rows 6-9, x 0-5) and the vertical band (cols 6-9, y 0-5);
    the 4x4 centre is the node and the arm end cap."""
    accent, accent_dim = ACCENTS[tier]
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    band = [EDGE, accent, accent_dim, EDGE]
    for i in range(16):
        for j in range(4):
            c = band[j]
            if j in (1, 2) and i % 4 == 3:
                c = mix(c, FRAME, 0.6)
            px[i, 6 + j] = c + (255,)
            px[6 + j, i] = c + (255,)
    for y in range(6, 10):
        for x in range(6, 10):
            ring = x in (6, 9) or y in (6, 9)
            px[x, y] = (FRAME if ring else accent) + (255,)
    px[6, 6] = px[9, 9] = FRAME_LO + (255,)
    px[9, 6] = px[6, 9] = FRAME_HI + (255,)
    for y in range(16):
        for x in range(16):
            if px[x, y][3] == 0:
                px[x, y] = CASING + (255,)
    return img


def main(out_dir):
    os.makedirs(os.path.join(out_dir, "pipe"), exist_ok=True)
    for tier in ACCENTS:
        panel_top(tier).save(os.path.join(out_dir, f"{tier}_solar_panel_top.png"))
        panel_side(tier).save(os.path.join(out_dir, f"{tier}_solar_panel_side.png"))
        pipe(tier).save(os.path.join(out_dir, "pipe", f"{tier}_energy_pipe.png"))
    panel_bottom().save(os.path.join(out_dir, "solar_panel_bottom.png"))


if __name__ == "__main__":
    main(sys.argv[1])
