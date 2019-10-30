# raceentry-api-client
Command line API client for RaceEntry - for downloading participants.  Makes for easier automated, scripted downloads.

Build with `./gradlew build`

Built app will be found in `build/distributions/raceentry-api-client-VERSION.zip`

Run `raceentry-api-client -h` for all options

Sample download from event id 1234 would be `./raceentry-api-client -e=1234 -u=USERNAME -p=PASSWORD -f=Database.csv`

Use the `--list` flag to find event id for the event you want to download `./raceentry-api-client -u=USERNAME -p=PASSWORD --list`
