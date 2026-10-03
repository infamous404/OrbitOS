# Administrator guide

*[Version française](../fr/admin-guide.md)*

How to run OrbitOS day to day. For the server itself (install, backups, upgrades), see the
[install guide](install.md).

## Who is who

| Role | Can | Typical person |
|---|---|---|
| Platform administrator | Create organizations, platform settings (email server, security, two-factor) | IT |
| Organization administrator | Everything inside one organization: devices, dashboards, rules, users, customers | Operations lead |
| Customer user | See the devices and dashboards assigned to their customer | A client, a building manager |

An **organization** (shown as "tenant" in some screens) is fully separate from the others. A
**customer** is a group inside an organization: give a client access to their own devices only.

## Users

- **Organization administrators**: platform administrator > *Tenants* > the organization > *Manage tenant admins*.
- **Customer users**: *Customers* > the customer > *Manage users*.
- New users get an activation email if an email server is set up (below); otherwise copy the
  activation link shown after creating the user and send it yourself.
- Each row has **Export personal data** and **Erase this account** for privacy requests
  (see [Privacy and security](privacy-security.md)).

## Devices

1. *Entities > Devices > +*. Give a name and, if useful, a **device profile** (a type, such as
   "thermostat", that groups settings and alarm rules).
2. Open the device and *Copy access token*: the device uses it to connect
   (see [Connect devices](connect-devices.md)).
3. *Check connectivity* on the device page shows ready-to-copy commands.
4. Assign a device to a customer to let that customer see it.

The **Latest telemetry** tab shows the last value of each measurement; **Attributes** shows the
device's properties (firmware version, location...).

<!-- #dashboards -->
## Dashboards

1. *Dashboards > +*, then *Edit* (pencil).
2. *Add widget*: charts, value cards, gauges, maps, tables, buttons that send commands.
3. Choose the data: the widget asks for an *entity alias* (which devices) and the *keys* (which
   measurements, for example `temperature`).
4. Save. Make a dashboard visible to a customer with *Assign to customer*, or public (read-only
   link) with *Make public*.

Map widgets show no background map while the server is offline by default; see
`ORBITOS_ONLINE_MAPS` in the [install guide](install.md).

<!-- #alarms -->
## Alarms

Alarms are set per device profile: *Profiles > Device profiles >* the profile *> Alarm rules*.
Example: "High temperature" when `temperature` is greater than 30 for 5 minutes, cleared when it
goes back under 28. Active alarms appear in *Alarms*, on dashboards, and as notifications.

<!-- #rules -->
## Rules

Every message from a device goes through the **root rule chain**: it saves values, saves
properties and handles commands. Open *Rule chains* to add steps, for example:

- filter or transform values (script node);
- send an email or a notification;
- call another service (REST API call node; see [webhooks](api.md));
- create alarms from complex conditions.

Change rule chains carefully: a broken root rule chain stops data from being saved. Export it
first (*Rule chains* > export) so you can restore it.

<!-- #notifications -->
## Email and notifications

- **Email server** (platform administrator): *Settings > Mail server*. Needed for activation and
  password-reset emails. Emails go out in each person's language.
- **Notifications**: *Notification center*. Default rules notify organization administrators
  about new alarms, device activity changes and failures. Each person chooses how to receive
  them (web, email) in their profile.

## Your settings

Top-right menu > *Profile*: name, language, password, two-factor sign-in.
