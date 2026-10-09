# Premium setup in Google Play Console

Premium is switched off in the code (`PREMIUM_ENABLED = false`) until these steps are done and a
test purchase worked end to end. While it is off, no screen mentions Premium and nothing is locked.

## 1. Prerequisites
- A Google Play developer account with a **payments profile** (merchant account) set up.
- The app uploaded at least once to a testing track (internal testing is enough). Products cannot be
  created before the first upload with the billing permission in the manifest (it is added by the
  Billing library automatically).
- Privacy policy and terms pages live at public URLs. Set them with
  `-Pzenflow.privacyUrl=...` and `-Pzenflow.termsUrl=...` (or in `gradle.properties`). The defaults in
  `app/build.gradle.kts` (`https://myzenflow.com/...`) are placeholders: **replace them with real pages**.

## 2. Create the products
**Monetize > Products**

| Product | Type | Product ID (default in `build.gradle.kts`) |
|---|---|---|
| Monthly | Subscription | `zenflow_premium_monthly` |
| Yearly | Subscription | `zenflow_premium_yearly` |
| Lifetime | One-time product | `zenflow_premium_lifetime` |

Subscriptions need a **base plan** (auto-renewing, monthly / yearly). Optionally add an **offer** with a
free trial (for example 7 days, "new customer acquisition"). The app shows the trial automatically only
for users Play says are eligible. Different ids can be used with `-Pzenflow.sku.monthly=...`,
`-Pzenflow.sku.yearly=...`, `-Pzenflow.sku.lifetime=...`.

Suggested starting prices (decide yourself, then adjust per country; Play converts and applies taxes):

| Plan | Suggestion | Notes |
|---|---|---|
| Monthly | about 4.99 USD | anchor price |
| Yearly | about 29.99 USD | about 50% cheaper than 12 months; the paywall shows the saving |
| Lifetime | about 59.99 USD | optional; remove `SKU_LIFETIME` plan if you do not want it |

## 3. Test
1. **Setup > License testing**: add your tester Google accounts. Their purchases are free and renew fast.
2. Build with premium on: `./gradlew installDebug -Pzenflow.premiumEnabled=true`
   (a debug build installs, but Play only returns products for an app signed like the uploaded one;
   use an internal-testing install, or upload a debug-signed build to internal testing, for real products).
3. Check: prices load, purchase sheet opens, purchase unlocks locked items, closing and reopening the app
   keeps Premium, **Restore purchases** works after reinstall, cancel keeps access until the period ends,
   refund/cancel removes Premium at the next app start (`refreshEntitlements`).
4. Check the failure paths: airplane mode (paywall shows retry), cancel in the purchase sheet (no error),
   pending payment (message, no unlock).

## 4. Go live
- Set `PREMIUM_ENABLED` to `true` by default in `app/build.gradle.kts` (or pass the property in your release build).
- Complete **Policy > App content** items for apps with in-app purchases (see `release_checklist.md`).
- Mention auto-renewal, price and cancellation in the store listing description (see `listing_en.md`).

## 5. Known limits (recommended follow-ups)
- Purchases are verified on the device only. For fraud/refund resilience add a small backend that
  verifies tokens with the Google Play Developer API and listens to Real-time developer notifications.
- Billing Library: the project uses `billing-ktx:8.0.0`. Google raises the minimum supported version
  every August; check the Play Console warnings and bump the version when asked.
- Grace period / account hold for subscriptions can be enabled in Play Console; the app treats anything
  other than a completed purchase as not premium.
