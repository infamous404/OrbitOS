# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
"""Patches the upstream IoT Gateway (Apache-2.0) inside the image: no calls home, no run-time installs.
Each change is an exact text replacement; the build fails if the upstream code changed."""
from pathlib import Path

ROOT = Path("/thingsboard_gateway")


def replace(relative, old, new):
    path = ROOT / relative
    text = path.read_text(encoding="utf-8")
    if text.count(old) != 1:
        raise SystemExit("OrbitOS gateway patch: expected text not found once in %s:\n%s" % (relative, old))
    path.write_text(text.replace(old, new), encoding="utf-8")
    print("patched", relative)


# 1. No update checks: upstream posts version, OS and an instance ID to its update server every hour.
replace("tb_utility/tb_updater.py",
        'UPDATE_SERVICE_BASE_URL = "https://updates.thingsboard.io"',
        'UPDATE_SERVICE_BASE_URL = ""  # OrbitOS: no update checks')
replace("tb_utility/tb_updater.py",
        "    def check_for_new_version(self):\n",
        "    def check_for_new_version(self):\n        return  # OrbitOS: no calls home\n")

# 2. Never download or upgrade a library that is already installed (upstream runs "pip install --upgrade"
#    for some connector libraries at every start). Libraries for Modbus, OPC UA and BACnet are in the image.
replace("tb_utility/tb_utility.py",
        '    def install_package(package, version="upgrade", force_install=False):\n',
        '    def install_package(package, version="upgrade", force_install=False):\n'
        '        if TBUtility.get_package_version(package) is not None:\n'
        '            return True  # OrbitOS: already installed, no download\n')
