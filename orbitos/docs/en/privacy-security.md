# Privacy and security controls

*[Version française](../fr/privacy-security.md)*

What OrbitOS does to protect accounts and personal information, and how administrators use it.
This guide is not legal advice. Have a privacy lawyer or your privacy officer review your use
(Law 25 in Québec, PIPEDA in Canada, GDPR, CCPA).

## Accounts and sign-in

| Control | Default | Where to change it |
|---|---|---|
| No default password | The platform administrator gets a random 20-character password, shown **once** at the end of setup | — |
| Forced change of that password | Must be replaced at the first sign-in | — |
| Password length | 12 characters minimum | Platform administrator: Security settings |
| Lockout | After 10 failed sign-ins | Security settings |
| Two-factor sign-in | Available to everyone (authenticator app, backup codes); not forced | Security settings > Two-factor authentication ("Enforce") |

### Turn on two-factor sign-in for your account

1. Open your profile (top right) > **Security**.
2. Turn on **Authenticator app** and scan the QR code with an app such as Aegis, Google
   Authenticator or Microsoft Authenticator. The codes are computed on your phone; no internet
   is needed on the server side.
3. Enter the 6-digit code to confirm. Also create **backup codes** and keep them somewhere safe.

To require it for everyone, the platform administrator turns on "Enforce two-factor
authentication" in Security settings.

## Audit log (tamper-evident)

Every audit entry (sign-ins, changes to devices, dashboards, users, rules...) is linked to the
previous one with a SHA-256 hash. If someone changes, deletes or inserts an entry directly in the
database, the chain no longer matches and the check says where.

Check it:

```bash
docker compose run --rm orbitos verify-audit
```

The result is `"status" : "intact"` (exit code 0) or `"broken"` with the entries concerned (exit
code 3). The platform administrator can also call `GET /api/orbitos/audit/verify`.

Limits:
- It detects tampering; it cannot prevent it. Someone with full database access could rebuild the
  whole chain. Keep a copy of the `latestHash` value somewhere else (for example in a weekly
  email to yourself): if a later check shows a different history before that point, the log was rebuilt.
- Entries removed by the retention period (below) are counted as `pruned`, not as tampering.
- User and target names are not part of the hash, so a privacy erasure can replace them with a pseudonym.

## Retention (keep data only as long as needed)

| Data | Kept for | Change it |
|---|---|---|
| Telemetry (device values) | 365 days | Organization profile ("Default storage TTL days"), or `SQL_TTL_TS_TS_KEY_VALUE_TTL` (seconds) |
| Alarms | 365 days | Organization profile ("Alarms TTL days") |
| Remote commands (RPC) | 30 days | Organization profile ("RPC TTL days") |
| Audit log | 365 days | `SQL_TTL_AUDIT_LOGS_SECS` in `.env` (seconds) |
| Rule engine events | 30 days (debug events: 7 days) | `SQL_TTL_EVENTS_EVENTS_TTL` |
| Notifications | 30 days | `SQL_TTL_NOTIFICATIONS_SECS` |

Old data is removed by background jobs (about once a day). Backups keep what they contained:
delete old backup files according to the same periods.

## Privacy requests (access, erasure)

For a person who has an account: in **Users** (customer users, or the organization
administrators list for the platform administrator), each row has two actions.

- **Export personal data**: downloads one JSON file with the account, sign-in status (never the
  password), preferences, actions performed by the person and about the account (audit log),
  notifications, and the customer record if the account belongs to one.
- **Erase this account**: deletes the account, its sign-in data, two-factor settings, preferences
  and notifications. Audit entries are kept for security for the retention period, but the
  person's name is replaced by a pseudonym such as `erased-user-3f9a1c0b2d`.

Rules: organization administrators handle accounts of their own organization only; nobody can
erase their own account or a platform administrator's account with this tool.

What is **not** covered, and needs a manual review by your privacy officer:
- Text the person typed inside audit details (for example in a device description they edited)
  stays until the audit retention period ends.
- Device data is not attributed to people by the platform. If a device measures a person (a
  wearable, a room sensor), handle it with the device's own data (delete the device or its telemetry).
- Copies in backups.
