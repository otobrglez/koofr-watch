# koofr-watch

[koofr-watch][koofr-watch] observes your Koofr storage / activities and emits changes to webhooks or similar making the
integration with other services and processes easier.

This product is not associated with Koofr organisation and was build by reverse-engineering [java-koofr] Java SDK.

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