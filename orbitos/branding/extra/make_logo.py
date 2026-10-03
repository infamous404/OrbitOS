"""Generate OrbitOS logo SVGs (vector redraw of the owner's PNG)."""
import math, os, sys

OUT = sys.argv[1]
BLUE_DEEP = "#1D5BFF"
BLUE = "#1E8BFF"
CYAN = "#35E0F7"
WHITE = "#FFFFFF"
NAVY = "#0B1A33"

# ---------- mark (64 x 64) ----------
CX, CY = 31.0, 33.0
R_RING, W_RING = 15.5, 7.5
RX, RY, ROT = 30.0, 8.2, -15.0
W_ORBIT = 3.0


def rot(x, y, deg=ROT, cx=CX, cy=CY):
    a = math.radians(deg)
    return (cx + x * math.cos(a) - y * math.sin(a), cy + x * math.sin(a) + y * math.cos(a))


def f(v):
    return f"{v:.2f}".rstrip("0").rstrip(".")


def arc_path(t0, t1, steps=48):
    pts = []
    for i in range(steps + 1):
        t = math.radians(t0 + (t1 - t0) * i / steps)
        pts.append(rot(RX * math.cos(t), RY * math.sin(t)))
    return "M" + " L".join(f"{f(x)} {f(y)}" for x, y in pts)


def mark(prefix, gid):
    # back arc: upper half (t 180..360), front arc: lower half (t 0..180)
    back = arc_path(180, 360)
    front = arc_path(-6, 186)
    d1 = rot(RX * math.cos(math.radians(150)), RY * math.sin(math.radians(150)))
    d2 = rot(RX * math.cos(math.radians(-55)), RY * math.sin(math.radians(-55)))
    return f"""
  <defs>
    <linearGradient id="{gid}" x1="0" y1="1" x2="1" y2="0">
      <stop offset="0" stop-color="{BLUE_DEEP}"/>
      <stop offset="0.55" stop-color="{BLUE}"/>
      <stop offset="1" stop-color="{CYAN}"/>
    </linearGradient>
    <mask id="{gid}-gap" maskUnits="userSpaceOnUse" x="-10" y="-10" width="100" height="100">
      <rect x="-10" y="-10" width="100" height="100" fill="#fff"/>
      <path d="{front}" fill="none" stroke="#000" stroke-width="{f(W_ORBIT + 3.2)}" stroke-linecap="round"/>
    </mask>
    <mask id="{gid}-behind" maskUnits="userSpaceOnUse" x="-10" y="-10" width="100" height="100">
      <rect x="-10" y="-10" width="100" height="100" fill="#fff"/>
      <circle cx="{f(CX)}" cy="{f(CY)}" r="{f(R_RING + W_RING / 2 + 1.6)}" fill="#000"/>
    </mask>
  </defs>
  <g{prefix}>
    <path d="{back}" fill="none" stroke="url(#{gid})" stroke-width="{f(W_ORBIT)}" stroke-linecap="round" mask="url(#{gid}-behind)"/>
    <circle cx="{f(CX)}" cy="{f(CY)}" r="{f(R_RING)}" fill="none" stroke="url(#{gid})" stroke-width="{f(W_RING)}" mask="url(#{gid}-gap)"/>
    <path d="{front}" fill="none" stroke="url(#{gid})" stroke-width="{f(W_ORBIT)}" stroke-linecap="round"/>
    <circle cx="{f(d1[0])}" cy="{f(d1[1])}" r="3.3" fill="{BLUE}"/>
    <circle cx="{f(d2[0])}" cy="{f(d2[1])}" r="3.6" fill="{CYAN}"/>
  </g>"""


# ---------- wordmark (cap height 30, stroke 5.5) ----------
S = 5.5
h = S / 2


def wordmark():
    x = 0.0
    parts_orbit, parts_os = [], []
    # O
    def big_o(x):
        return f'<rect x="{f(x + h)}" y="{f(h)}" width="{f(32 - S)}" height="{f(30 - S)}" rx="11.5"/>', x + 32
    o, x = big_o(x)
    parts_orbit.append(o)
    x += 5.5
    # r
    xs = x + h
    parts_orbit.append(f'<path d="M{f(xs)} 30 V21.5 A9.75 9.75 0 0 1 {f(xs + 9.75)} {f(9 + h)} H{f(xs + 12.5)}"/>')
    x = xs + 12.5 + 4.5
    # b
    xb = x + h
    parts_orbit.append(f'<path d="M{f(xb)} 0 V30"/>')
    w = 19.0
    r = (30 - h - (9 + h)) / 2
    parts_orbit.append(
        f'<path d="M{f(xb)} {f(9 + h)} H{f(xb + w - r)} A{f(r)} {f(r)} 0 0 1 {f(xb + w - r)} {f(30 - h)} H{f(xb)}"/>')
    x = xb + w + h + 5.5
    # i
    xi = x + h
    parts_orbit.append(f'<path d="M{f(xi)} 9 V30"/>')
    dots = [f'<circle cx="{f(xi)}" cy="3.1" r="3.1"/>']
    x = xi + h + 5.0
    # t
    xt = x + 4 + h
    parts_orbit.append(f'<path d="M{f(xt)} 1.5 V22.25 A5 5 0 0 0 {f(xt + 5)} {f(30 - h)} H{f(xt + 8.5)}"/>')
    parts_orbit.append(f'<path d="M{f(xt - 4.5)} {f(9 + h)} H{f(xt + 8)}"/>')
    x = xt + 8.5 + 5.5
    # O
    o, x = big_o(x)
    parts_os.append(o)
    x += 5.5
    # S
    W = 24.0
    rr = (30 - S) / 4
    a = x + h + rr
    b = x + W - h - rr
    parts_os.append(
        f'<path d="M{f(x + W - h + 0.5)} {f(h)} H{f(a)} A{f(rr)} {f(rr)} 0 0 0 {f(a)} 15 H{f(b)} '
        f'A{f(rr)} {f(rr)} 0 0 1 {f(b)} {f(30 - h)} H{f(x + h - 0.5)}"/>')
    x += W
    return parts_orbit, dots, parts_os, x


def wordmark_svg(orbit_color, gid, dx, dy, scale):
    po, dots, pos_, width = wordmark()
    stroke = f'fill="none" stroke-width="{f(S)}" stroke-linecap="butt" stroke-linejoin="round"'
    return f"""
  <g transform="translate({f(dx)} {f(dy)}) scale({f(scale)})">
    <g {stroke} stroke="{orbit_color}">{''.join(po)}</g>
    <g fill="{orbit_color}">{''.join(dots)}</g>
    <g {stroke} stroke="url(#{gid}-w)">{''.join(pos_)}</g>
  </g>""", width


def wordmark_gradient(gid, x0, x1):
    return f"""
  <defs>
    <linearGradient id="{gid}-w" gradientUnits="userSpaceOnUse" x1="{f(x0)}" y1="30" x2="{f(x1)}" y2="0">
      <stop offset="0" stop-color="{BLUE_DEEP}"/>
      <stop offset="1" stop-color="{CYAN}"/>
    </linearGradient>
  </defs>"""


def horizontal(orbit_color, name):
    _, _, _, width = wordmark()
    scale = 0.9
    wx = 72
    total = wx + width * scale + 2
    body, _ = wordmark_svg(orbit_color, "ow", wx, 18.5, scale)
    os_start = width - 61
    svg = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {f(total)} 64" role="img" aria-label="OrbitOS">{mark('', 'om')}{wordmark_gradient('ow', os_start, width)}{body}
</svg>
"""
    open(os.path.join(OUT, name), "w", encoding="utf-8").write(svg)


def stacked(orbit_color, name):
    _, _, _, width = wordmark()
    scale = 1.0
    total_w = max(width * scale, 64 * 2.2) + 16
    mark_scale = 2.2
    mx = (total_w - 64 * mark_scale) / 2
    body, _ = wordmark_svg(orbit_color, "sw", (total_w - width * scale) / 2, 64 * mark_scale + 4, scale)
    os_start = width - 61
    svg = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {f(total_w)} {f(64 * mark_scale + 4 + 30 + 10)}" role="img" aria-label="OrbitOS">
  <g transform="translate({f(mx)} 0) scale({f(mark_scale)})">{mark('', 'sm')}
  </g>{wordmark_gradient('sw', os_start, width)}{body}
</svg>
"""
    open(os.path.join(OUT, name), "w", encoding="utf-8").write(svg)


def icon(name, background):
    bg = f'\n  <rect width="64" height="64" rx="14" fill="{NAVY}"/>' if background else ""
    svg = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" role="img" aria-label="OrbitOS">{bg}{mark('', 'fm')}
</svg>
"""
    open(os.path.join(OUT, name), "w", encoding="utf-8").write(svg)


horizontal(WHITE, "logo.svg")
horizontal(NAVY, "logo-light.svg")
stacked(WHITE, "logo-stacked-dark.svg")
stacked(NAVY, "logo-stacked-light.svg")
icon("favicon.svg", True)
icon("mark.svg", False)
print("ok")
