# koofr-watch

[koofr-watch][koofr-watch] observes your [Koofr](https://koofr.eu/) storage/activities and emits changes to webhooks
making the integration with other services and processes a breeze!

This product is not associated with the Koofr organisation and was built by reverse-engineering [java-koofr] Java SDK.

Koofr [API Documentation is here](https://stage.koofr.net/developers/api).

## Development

Get your application
password [here](https://koofr.eu/help/linking-koofr-with-desktops/how-to-generate-an-application-specific-password-in-koofr/).

```bash
export KOOFR_PASSWORD="<your_koofer_password>" 
export KOOFR_USERNAME="<your_koofer_username>"
export PORT=4443
export WEBHOOK_URL_1=https://discord.com/api/webhooks/123.....
export WEBHOOK_URL_2=https://discord.com/api/webhooks/456.....
export WEBHOOK_URL_3=https://discord.com/api/webhooks/768..... # Five of them are supported....

sbt run
```

## Author

\- [Oto Brglez](https://github.com/otobrglez)

[koofr-watch]: https://github.com/otobrglez/koofr-watch

[java-koofr]: https://github.com/koofr/java-koofr
