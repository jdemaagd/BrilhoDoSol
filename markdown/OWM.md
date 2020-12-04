# [Open Weather Map](https://openweathermap.org/api)

## Endpoints

- https://api.openweathermap.org/data/2.5/forecast?q=Ann%20Arbor%2C%20MI%2048104&mode=json&units=metric&cnt=14&appid=
- https://api.openweathermap.org/data/2.5/forecast?lat=42.278210&lon=-83.745670&mode=json&units=metric&cnt=14&appid=app_key=

## Refresh

- Eliminate refresh from Production App
- With ability to run background tasks or send messages directly from server to app
  - no reason to force users to refresh
  - that app is up to date and synced with cloud is given
- Also means AsyncTask is not desirable in Activity
  - coupling background operation (network call) with a foreground component (Activity)

## Resources

- [OWM API Keys](https://home.openweathermap.org/api_keys)

- [Input Stream](https://stackoverflow.com/questions/309424/how-do-i-read-convert-an-inputstream-into-a-string-in-java)
- [OkHttp](https://square.github.io/okhttp/)
- [Open Weather API](https://openweathermap.org/current)
- [OWM Url]()

