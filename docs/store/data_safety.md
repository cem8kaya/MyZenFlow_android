# Data safety form: suggested answers (verify before submitting)

Based on the code in this repository: no network calls except Google Play Billing, no analytics, no ads,
no accounts, no third-party SDKs that collect data. If you add Crashlytics, analytics or a backend,
update this form and the privacy policy.

| Question | Answer |
|---|---|
| Does the app collect or share any of the required user data types? | **No**, data stays on the device. (Declare differently if you add analytics, crash reporting or a server.) |
| Is all collected data encrypted in transit? | Not applicable (no data leaves the device). |
| Do you provide a way for users to request data deletion? | Data is local. Uninstalling or "Clear storage" deletes it. State this in the policy. |
| Data locally stored | Name (optional), practice sessions, mood check-ins, settings, achievements. Android Auto Backup may copy the app data to the user's own Google account (system feature). |
| Purchases | Handled by Google Play. The app only reads purchase status. |
| Target audience | 13+ (not designed for children). Do not enroll in Designed for Families. |
| Ads | None. |

Other App content declarations to complete in Play Console: Privacy policy URL, Ads (no), App access
(no login needed), Content rating questionnaire (IARC; select a wellness/health reference app, no
user-generated content, no violence), Target audience (13+/18+), News app (no), Government app (no),
**Health apps** declaration (the app is a wellness / mindfulness app, not a medical app; make no medical claims),
Foreground service permissions (see below).
