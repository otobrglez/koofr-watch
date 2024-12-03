# koofr-watch

[koofr-watch][koofr-watch] observes your [Koofr](https://koofr.eu/) storage/activities and emits changes to webhooks or similar, making the integration with other services and processes easier.

This product is not associated with the Koofr organisation and was built by reverse-engineering [java-koofr] Java SDK.

Koofr [API Documentation is here](https://stage.koofr.net/developers/api).

## Development

```bash
export KOOFR_USERNAME="<your_koofer_username>"

# Get it here: https://koofr.eu/help/linking-koofr-with-desktops/how-to-generate-an-application-specific-password-in-koofr/
export KOOFR_PASSWORD="<your_koofer_password>" 
export PORT=4443

sbt run
```

## Author

\- [Oto Brglez](https://github.com/otobrglez)

[koofr-watch]: https://github.com/otobrglez/koofr-watch

[java-koofr]: https://github.com/koofr/java-koofr
