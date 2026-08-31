# Copy Stand for Android

A clipboard manager application that can synchronize your device's clipboard
automatically with other devices on your network.

## Ports

This application is intended to work together with applications on other
platforms to provide the important synchronization feature. Here are all the
supported platforms:

  - [Windows](https://github.com/nathanpc/copy-stand-windows)

## Building

Since this application is intended to be built for older platforms, it is
required to use [Android Studio Narwhal 4 Feature Drop | 2025.1.4](https://developer.android.com/studio/archive).

## Android 10+ Limitations

Unfortunately, due to a [security limitation introduced in Android 10](https://developer.android.com/privacy-and-security/risks/secure-clipboard-handling)
that has no workaround or user-facing bypass, the application cannot access the
system's clipboard while running on the background, making clipboard manager
applications impossible to exist without modifications to the system.

Since Google does not provide any way for a user to grant permissions to
specific applications to access the clipboard on the background, in order to
use this application **root is required**.

You must install the [Clipboard Whitelist](https://github.com/Kr328/Riru-ClipboardWhitelist)
module in [Magisk](https://github.com/topjohnwu/Magisk) and use it to whitelist
the Copy Stand application.

If you're running a version of Android that's below Q (10), root is not required
for the application to work.

## License

This application is free software; you may redistribute and/or modify it under 
the terms of the [Mozilla Public License 2.0](https://www.mozilla.org/en-US/MPL/2.0/).
