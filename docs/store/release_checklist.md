# Release checklist

## Build
- [ ] `targetSdk` meets the current Play requirement (the project targets API 36; check Play Console warnings each August).
- [ ] `versionCode` increased (`-PversionCode=N`), `versionName` set (`-PversionName=1.0.0`).
- [ ] Signed with your upload key (`keystore.properties`, see `RELEASE_GUIDE.md`), Play App Signing enabled.
- [ ] `./gradlew bundleRelease -PversionCode=N` and upload the `.aab`.
- [ ] `./gradlew testDebugUnitTest` passes (includes localization consistency, streak, entitlement tests).
- [ ] Generate a real **Baseline Profile** (the current `baseline-prof.txt` is a hand-written starter).
- [ ] Premium decision made: `PREMIUM_ENABLED` and Play Console products (`play_console_setup.md`).
- [ ] Privacy policy and terms URLs are real (`zenflow.privacyUrl`, `zenflow.termsUrl`).

## Play Console: App content
- [ ] Privacy policy URL
- [ ] Data safety form (`data_safety.md`)
- [ ] Content rating questionnaire
- [ ] Target audience: 13+ (not Designed for Families)
- [ ] Health apps declaration (wellness app, no medical claims)
- [ ] Ads: none
- [ ] **Foreground service** declaration for `specialUse`. Suggested text: "User-started Pomodoro focus/break
      countdown that must keep running while the screen is off; shows an ongoing notification with pause,
      resume and stop controls. Ends when the timer ends or the user stops it." Provide a short video of
      starting the timer and the notification.
- [ ] Permissions used: POST_NOTIFICATIONS (reminders and timer alerts, requested in context), VIBRATE,
      WAKE_LOCK, FOREGROUND_SERVICE (+ SPECIAL_USE). No exact-alarm, location, contacts, storage or microphone permissions.

## Store listing
- [ ] Title, short and full description per language (`listing_*.md`), medical disclaimer kept
- [ ] App icon 512x512, feature graphic 1024x500
- [ ] Phone screenshots (min 2, 16:9 or 9:16), 7" and 10" tablet screenshots (important: the app adapts to tablets)
- [ ] Suggested screenshot order: Home with recommendation, Breathing in progress, Focus timer, Zen Garden (night/day), Weekly summary, Paywall (only if Premium is on)
- [ ] Localized screenshots for en and tr at least
- [ ] Short promo video (optional)

## Testing tracks
- [ ] Internal testing: install via Play, test purchase flows with license testers
- [ ] Closed testing: new personal developer accounts must run a closed test with enough testers for the
      required number of days before production (check the current requirement in Play Console)
- [ ] Review the **Pre-launch report** (crashes, accessibility, security)

## After launch
- [ ] Watch Android vitals (crash and ANR rates) in Play Console
- [ ] Answer reviews; collect feedback for the next version
