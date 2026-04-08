# WhichBrowser BETA

An Android app that gives **you** back control — choose which mobile browser to use for any specific domain.

Tired of apps forcing you into Chrome, or always opening links in the default browser?  
WhichBrowser lets you set per-domain browser preferences so links open exactly where you want them.

## Features

- Per-domain browser selection
- Currently works with Chrome, Edge, DuckDuckGo (TODO - make dynamic)
- Simple and lightweight
- Quick domain management (TODO)

## Screenshots

*TODO*

| Main Screen | Domain List | Browser Picker |
|-------------|-------------|----------------|
| ![Main](screenshots/main.png) | ![Domains](screenshots/domains.png) | ![Picker](screenshots/picker.png) |

## How to Use

1. Install the app
2. Navigate to Settings : All apps : Browser app
3. Choose WhichBrowser / "Yes" (make it your default browser)
4. Make sure you have at least Chrome, Edge and DuckDuckGo installed
5. Navigate to a webpage
6. Choose which (real) browser to use. If checkbox is checked, future links to that domain will open in your chosen browser automatically

## Tech Stack

- Kotlin
- Android Jetpack (ViewModel, Room, Navigation Component)
- Modern Android architecture

## Installation

### For Users

Download the latest APK from the [Releases](https://github.com/bheckel/WhichBrowser/releases) page.

### For Developers

```bash
git clone git@github.com:bheckel/WhichBrowser.git
cd WhichBrowser
```
Open the project in Android Studio
Sync Gradle and run on your device or emulator.

### Contributing
- Contributions are welcome! Feel free to open issues or submit pull requests.

### License
- MIT License
