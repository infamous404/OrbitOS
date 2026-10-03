# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
"""Generates the sample dashboard created by the first-run setup page.

Built from upstream (Apache-2.0) widget configurations in this repository, restyled with the
OrbitOS accents (dark panel, cyan lines, monospace values). Run from the repository root:
    python orbitos/scripts/make_first_run_dashboard.py
Placeholders (__TITLE__, __DEVICE_ID__, ...) are filled in by FirstRunSetupService.
"""
import copy
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2] / 'application/src/main'
OUT = ROOT / 'resources/orbitos/first-run/dashboard.json'

BG = '#0B1220'        # page
CARD = '#111A2E'      # widget
CYAN = '#22D3EE'      # accent (line)
CYAN_TEXT = '#67E8F9' # values on dark: contrast > 10:1
VIOLET = '#A78BFA'
TEXT = '#E2E8F0'
MUTED = '#94A3B8'     # contrast on CARD > 6:1
GRID = 'rgba(148, 163, 184, 0.15)'
MONO = 'Roboto Mono, Consolas, monospace'
CARD_STYLE = {
    'border': '1px solid rgba(34, 211, 238, 0.35)',
    'borderRadius': '6px',
    'boxShadow': '0 0 14px rgba(34, 211, 238, 0.12)',
}
ALIAS = 'b6a0c1f2-0d1e-4c3b-9a7e-5f2d8c4e1a01'


def value_card(wid, key, label_ph, icon, units, color):
    wt = json.loads((ROOT / 'data/json/system/widget_types/value_card.json').read_text(encoding='utf-8'))
    cfg = json.loads(wt['descriptor']['defaultConfig'])
    cfg['datasources'] = [{
        'type': 'entity', 'name': '', 'entityAliasId': ALIAS, 'filterId': None,
        'dataKeys': [{'name': key, 'type': 'timeseries', 'label': label_ph, 'color': color,
                      'settings': {}, '_hash': 0.1, 'units': units, 'decimals': 1}],
        'alarmFilterConfig': {'statusList': ['ACTIVE']},
    }]
    cfg['units'] = units
    cfg['decimals'] = 1
    cfg['title'] = label_ph
    cfg['backgroundColor'] = CARD
    cfg['color'] = TEXT
    cfg['widgetStyle'] = CARD_STYLE
    s = cfg['settings']
    s['labelColor']['color'] = MUTED
    s['labelFont']['family'] = MONO
    s['labelFont']['size'] = 13
    s['labelFont']['weight'] = '500'
    s['icon'] = icon
    s['iconColor']['color'] = color
    s['valueColor']['color'] = CYAN_TEXT if color == CYAN else color
    s['valueFont']['family'] = MONO
    s['dateColor']['color'] = MUTED
    s['dateFont']['family'] = MONO
    s['background'] = {'type': 'color', 'color': CARD, 'overlay': {'enabled': False, 'color': 'rgba(0,0,0,0)', 'blur': 3}}
    return {'typeFullFqn': 'system.cards.value_card', 'type': 'latest', 'sizeX': 4, 'sizeY': 3,
            'config': cfg, 'row': 0, 'col': 0, 'id': wid}


def chart(wid):
    demo = json.loads((ROOT / 'data/json/demo/dashboards/thermostats.json').read_text(encoding='utf-8'))
    src = next(w for w in demo['configuration']['widgets'].values() if w.get('typeFullFqn') == 'system.time_series_chart')
    w = copy.deepcopy(src)
    cfg = w['config']
    ds = cfg['datasources'][0]
    ds['entityAliasId'] = ALIAS
    ds['latestDataKeys'] = []
    base_key = ds['dataKeys'][0]
    keys = []
    for name, label, color, units in (('temperature', '__TEMPERATURE__', CYAN, '°C'), ('humidity', '__HUMIDITY__', VIOLET, '%')):
        k = copy.deepcopy(base_key)
        k.update({'name': name, 'label': label, 'color': color, 'units': units, 'decimals': 1, '_hash': 0.2})
        k['settings']['lineSettings']['pointLabelColor'] = TEXT
        k['settings']['lineSettings']['fillAreaSettings'] = {'type': 'gradient', 'opacity': 0.25, 'gradient': {'start': 40, 'end': 0}}
        keys.append(k)
    ds['dataKeys'] = keys
    cfg['title'] = '__CHART__'
    cfg['backgroundColor'] = CARD
    cfg['color'] = TEXT
    cfg['titleColor'] = TEXT
    cfg['titleFont']['family'] = MONO
    cfg['widgetStyle'] = CARD_STYLE
    cfg['timewindowStyle']['color'] = MUTED
    cfg['useDashboardTimewindow'] = True
    s = cfg['settings']
    s['thresholds'] = []
    for axis in ('yAxis', 'xAxis'):
        a = s[axis]
        a['labelColor'] = MUTED
        a['tickLabelColor'] = MUTED
        a['ticksColor'] = GRID
        a['lineColor'] = GRID
        a['splitLinesColor'] = GRID
        a['tickLabelFont']['family'] = MONO
    s['legendLabelColor'] = TEXT
    s['legendLabelFont']['family'] = MONO
    for k in ('legendValueColor', 'legendColumnTitleColor'):
        if k in s:
            s[k] = MUTED
    s['tooltipValueColor'] = TEXT
    s['tooltipDateColor'] = MUTED
    s['tooltipBackgroundColor'] = 'rgba(11, 18, 32, 0.92)'
    s['background'] = {'type': 'color', 'color': CARD, 'overlay': {'enabled': False, 'color': 'rgba(0,0,0,0)', 'blur': 3}}
    w['id'] = wid
    w['sizeX'], w['sizeY'] = 16, 7
    return w


ids = {'t': '3e0f8a5c-6b1d-4f2a-8c9e-7d4b2a1f0c11', 'h': '3e0f8a5c-6b1d-4f2a-8c9e-7d4b2a1f0c12', 'c': '3e0f8a5c-6b1d-4f2a-8c9e-7d4b2a1f0c13'}
widgets = {
    ids['t']: value_card(ids['t'], 'temperature', '__TEMPERATURE__', 'thermostat', '°C', CYAN),
    ids['h']: value_card(ids['h'], 'humidity', '__HUMIDITY__', 'water_drop', '%', VIOLET),
    ids['c']: chart(ids['c']),
}
layout = {
    ids['t']: {'sizeX': 8, 'sizeY': 4, 'row': 0, 'col': 0},
    ids['h']: {'sizeX': 8, 'sizeY': 4, 'row': 4, 'col': 0},
    ids['c']: {'sizeX': 16, 'sizeY': 8, 'row': 0, 'col': 8},
}
dashboard = {
    'title': '__TITLE__',
    'configuration': {
        'description': '',
        'widgets': widgets,
        'states': {'default': {'name': '__TITLE__', 'root': True, 'layouts': {'main': {
            'widgets': layout,
            'gridSettings': {'backgroundColor': BG, 'color': TEXT, 'columns': 24, 'backgroundSizeMode': '100%',
                             'autoFillHeight': True, 'mobileAutoFillHeight': False, 'mobileRowHeight': 70,
                             'margin': 12, 'outerMargin': True, 'layoutType': 'default'}}}}},
        'entityAliases': {ALIAS: {'id': ALIAS, 'alias': '__DEVICE_NAME__', 'filter': {
            'type': 'singleEntity', 'resolveMultiple': False,
            'singleEntity': {'entityType': 'DEVICE', 'id': '__DEVICE_ID__'}}}},
        'filters': {},
        'timewindow': {'hideAggregation': False, 'hideAggInterval': False, 'hideTimezone': False, 'selectedTab': 0,
                       'realtime': {'realtimeType': 0, 'timewindowMs': 3600000, 'interval': 10000},
                       'aggregation': {'type': 'NONE', 'limit': 2000}},
        'settings': {'stateControllerId': 'entity', 'showTitle': False, 'showDashboardsSelect': True,
                     'showEntitiesSelect': True, 'showDashboardTimewindow': True, 'showDashboardExport': True,
                     'toolbarAlwaysOpen': True, 'titleColor': TEXT, 'showDashboardLogo': False,
                     'hideToolbar': False, 'showFilters': True, 'showUpdateDashboardImage': False,
                     # Small detail: a thin cyan scan line under the dashboard title bar.
                     'dashboardCss': '.tb-dashboard-page .tb-dashboard-toolbar { border-bottom: 1px solid rgba(34, 211, 238, 0.4); }'},
    },
}
OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps(dashboard, ensure_ascii=False, indent=1) + '\n', encoding='utf-8', newline='\n')
print('written', OUT, OUT.stat().st_size)
