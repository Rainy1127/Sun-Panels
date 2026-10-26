"""Draws the Sun Panels logo: an isometric solar panel with a neon grid under a rising neon-orange sun.

Usage: python3 tools/gen_logo.py <output dir>
Writes logo_icon.png (512, symbol only), logo_named.png (512, symbol + name) and logo.png (128, for the jar).
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFilter, ImageFont

SS = 4
SIZE = 512
N = SIZE * SS

BG_TOP = (16, 16, 22)
BG_BOTTOM = (6, 6, 9)
EDGE = (40, 40, 54)
CYAN = (0, 240, 255)
ORANGE = (255, 159, 28)
CELL_LIGHT = (30, 84, 150)
CELL_DARK = (8, 26, 58)
CASE_LEFT = (34, 37, 47)
CASE_RIGHT = (22, 24, 31)

FONT_CANDIDATES = [
    "/usr/share/fonts/opentype/inter/InterDisplay-Bold.otf",
    "/usr/share/fonts/truetype/google-fonts/Poppins-Bold.ttf",
    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
]


def lerp(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(len(a)))


def background():
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    grad = Image.new("RGBA", (N, N))
    gd = ImageDraw.Draw(grad)
    for y in range(N):
        gd.line([(0, y), (N, y)], fill=lerp(BG_TOP, BG_BOTTOM, y / N) + (255,))
    mask = Image.new("L", (N, N), 0)
    radius = int(N * 0.2)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, N - 1, N - 1], radius=radius, fill=255)
    img.paste(grad, (0, 0), mask)
    ImageDraw.Draw(img).rounded_rectangle([SS * 3, SS * 3, N - 1 - SS * 3, N - 1 - SS * 3], radius=radius - SS * 3,
                                          outline=EDGE + (255,), width=SS * 3)
    return img, mask


def glow(layer, radius):
    blurred = layer.filter(ImageFilter.GaussianBlur(radius))
    out = Image.new("RGBA", layer.size, (0, 0, 0, 0))
    out.alpha_composite(blurred)
    out.alpha_composite(blurred)
    out.alpha_composite(layer)
    return out


def sun(cx, cy, r):
    layer = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    for i in range(12):
        a = math.pi * 2 * i / 12
        r0, r1 = r * 1.22, r * (1.48 if i % 2 == 0 else 1.36)
        d.line([(cx + math.cos(a) * r0, cy + math.sin(a) * r0), (cx + math.cos(a) * r1, cy + math.sin(a) * r1)],
               fill=ORANGE + (255,), width=int(r * 0.09))
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=ORANGE + (255,))
    d.ellipse([cx - r * 0.72, cy - r * 0.72, cx + r * 0.72, cy + r * 0.72], fill=(255, 196, 90, 255))
    return glow(layer, r * 0.35)


def iso(cx, cy, w, h, u, v):
    """Point on the isometric top face: u, v in 0..1 along the two edges."""
    return (cx + (u - v) * w / 2, cy - h / 2 + (u + v) * h / 2)


def panel(cx, cy, w, h, depth):
    layer = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    top = [iso(cx, cy, w, h, 0, 0), iso(cx, cy, w, h, 1, 0), iso(cx, cy, w, h, 1, 1), iso(cx, cy, w, h, 0, 1)]
    left = [top[3], top[2], (top[2][0], top[2][1] + depth), (top[3][0], top[3][1] + depth)]
    right = [top[2], top[1], (top[1][0], top[1][1] + depth), (top[2][0], top[2][1] + depth)]
    d.polygon(left, fill=CASE_LEFT + (255,))
    d.polygon(right, fill=CASE_RIGHT + (255,))

    cells = 3
    for i in range(cells):
        for j in range(cells):
            t = (i + j) / (2 * (cells - 1))
            quad = [iso(cx, cy, w, h, i / cells, j / cells), iso(cx, cy, w, h, (i + 1) / cells, j / cells),
                    iso(cx, cy, w, h, (i + 1) / cells, (j + 1) / cells), iso(cx, cy, w, h, i / cells, (j + 1) / cells)]
            d.polygon(quad, fill=lerp(CELL_LIGHT, CELL_DARK, t) + (255,))
    # glare across the cells
    glare = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glare)
    gd.polygon([iso(cx, cy, w, h, 0.05, 0.0), iso(cx, cy, w, h, 0.35, 0.0),
                iso(cx, cy, w, h, 0.0, 0.62), iso(cx, cy, w, h, 0.0, 0.32)], fill=(170, 215, 255, 46))
    clip = Image.new("L", (N, N), 0)
    ImageDraw.Draw(clip).polygon(top, fill=255)
    layer.paste(Image.alpha_composite(layer, glare), (0, 0), clip)

    lines = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    ld = ImageDraw.Draw(lines)
    lw = int(N * 0.008)
    for k in range(1, cells):
        ld.line([iso(cx, cy, w, h, k / cells, 0), iso(cx, cy, w, h, k / cells, 1)], fill=CYAN + (255,), width=lw)
        ld.line([iso(cx, cy, w, h, 0, k / cells), iso(cx, cy, w, h, 1, k / cells)], fill=CYAN + (255,), width=lw)
    ld.line(top + [top[0]], fill=CYAN + (255,), width=int(lw * 1.6), joint="curve")
    ld.line([top[3], top[2], top[1]], fill=CYAN + (255,), width=int(lw * 1.6), joint="curve")
    layer.alpha_composite(glow(lines, N * 0.012))
    return layer


def symbol(scale, offset_y):
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    img.alpha_composite(sun(N * 0.5, N * (0.36 + offset_y), N * 0.15 * scale))
    img.alpha_composite(panel(N * 0.5, N * (0.62 + offset_y), N * 0.78 * scale, N * 0.39 * scale, N * 0.075 * scale))
    return img


def font(px):
    for path in FONT_CANDIDATES:
        if os.path.exists(path):
            return ImageFont.truetype(path, px)
    return ImageFont.load_default()


def render(named):
    img, mask = background()
    if named:
        img.alpha_composite(symbol(0.86, -0.07))
        d = ImageDraw.Draw(img)
        f = font(int(N * 0.115))
        text = "SUN PANELS"
        tw = d.textlength(text, font=f)
        text_layer = Image.new("RGBA", (N, N), (0, 0, 0, 0))
        ImageDraw.Draw(text_layer).text(((N - tw) / 2, N * 0.775), text, font=f, fill=(235, 250, 255, 255))
        img.alpha_composite(glow(text_layer, N * 0.006))
    else:
        img.alpha_composite(symbol(1.0, -0.03))
    clipped = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    clipped.paste(img, (0, 0), mask)
    return clipped.resize((SIZE, SIZE), Image.LANCZOS)


def main(out_dir):
    os.makedirs(out_dir, exist_ok=True)
    icon = render(False)
    icon.save(os.path.join(out_dir, "logo_icon.png"))
    render(True).save(os.path.join(out_dir, "logo_named.png"))
    icon.resize((128, 128), Image.LANCZOS).save(os.path.join(out_dir, "logo.png"))


if __name__ == "__main__":
    main(sys.argv[1])
