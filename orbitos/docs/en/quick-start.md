# Quick start

*[Version française](../fr/quick-start.md)*

From zero to a live dashboard. You need a computer with **Docker** (Docker Desktop on Windows or
macOS) and about 4 GB of free memory for the first build.

## 1. Start OrbitOS

In the OrbitOS folder:

```bash
docker compose up -d
```

The first time, this builds OrbitOS from source (15 to 30 minutes). Later starts take a minute.
With a ready-made image, see "Use a published image" in the [install guide](install.md).

## 2. Set it up

1. Open **http://localhost:8080**. The setup page opens.
2. Choose your language.
3. Get the setup code: `docker compose logs orbitos`, look for "First-run setup".
4. Enter your organization name, your email and a password (12 characters or more).
5. Keep "Create a sample device and dashboard" checked and click **Finish setup**.
6. **Save the platform administrator password** shown at the end (it is shown only once).

## 3. Send data

Click **Send a test value** on the last setup step, or send data like a real device:

```bash
python orbitos/scripts/mqtt_device.py --token <device token from the setup page>
```

## 4. See it

Click **Sign in and open the dashboard**. The values appear as they arrive.

## Next

- Add your own devices: [Connect devices](connect-devices.md).
- Invite people, build dashboards, set alarms: [Administrator guide](admin-guide.md).
- Turn on two-factor sign-in: [Privacy and security](privacy-security.md).
- Back up regularly: [Install, back up, upgrade](install.md).
