# Autonomy Android

Autonomy is a mobile app that helps citizens and institutions with COVID-19

[![Made by](https://img.shields.io/badge/Made%20by-Bitmark%20Inc-lightgrey.svg)](https://bitmark.com)

## Getting Started

#### Prequisites

- Java 8
- Android 6.0 (API 23)

#### Preinstallation

Create `.properties` file for the configuration
- `sentry.properties` : uploading the Proguard mapping file to Sentry
```xml
defaults.project=autonomy-android
defaults.org=bitmark-inc
auth.token=SentryAuthToken
```
- `key.properties` : API key configuration
```xml
api.key.bitmark=BitmarkSdkApiKey
api.key.intercom=IntercomApiKey
```
- `app/src/main/resources/sentry.properties` : Configuration for Sentry
```xml
dsn=SentryDSN
buffer.dir=sentry-events
buffer.size=100
async=true
async.queuesize=100
```
- `app-center.properties` : Configuration for app center uploading
```xml
api-key=AppCenterToken
```

#### Signing
Add `release.keystore` and `release.properties` for releasing as production

#### Installing

`./gradlew clean fillSecretKey assembleInhouseDebug`

Using `-PsplitApks` to build split APKs

## Deployment
`./gradlew appCenterUploadInhouseDebug`
