About
--------------------------------
OpenSMIME is an open-source Android mobile application designed to manage SMIME certificates on Android. It interacts with the Contacts feature and acts as a plug-in for [K-9](https://github.com/k9mail/k-9). It is designed to function similarly to [OpenKeychain](https://github.com/open-keychain/open-keychain).

It is a fork of SMile Cryptographic Extension (SMileCE), a mobile application developed by students during the course "Mobile Application Development" (MAD) at Friedrich-Alexander University Erlangen-Nürnberg, Germany.

OpenSMIME provides a variety of useful features like listing imported certificates, show additional information of the certificates and create own self-signed certificates.

Audit
--------------------------------
Currently OpenSMIME is under continual development, as such there has been and is no auditing planned. Prior to public release we will contact interested parties for a code audit (subject to cost, etc).


License
--------------------------------
OpenSMIME is developed under the Apache License Version 2.0.

Included projects
--------------------------------
Tools and Libraries that we used:

- [kore-javamail](https://github.com/konradrenner/kore-javamail), License: GNU GPL v2
- [Material Design Icons](https://www.google.com/design/icons/), License: CC BY 4.0
- [Polygon Background](http://blog.spoongraphics.co.uk), License: CC BY 2.0
- [Spongycastle](https://github.com/FAU-Inf2/spongycastle), License: Adaption of MIT X11 License
- [aFileChooser](https://github.com/iPaulPro/aFileChooser), License: Apache License 2.0
- [joda-time-android](https://github.com/dlew/joda-time-android), License: Apache License 2.0
- [Apache Commons IO](https://commons.apache.org/proper/commons-io/), License: Apache License 2.0
- [Android Swipe Layout](https://github.com/daimajia/AndroidSwipeLayout), License: MIT 

How-To use OpenSMIME with K-9
--------------------------------
- Install a development snapshot build of K-9
- Open K-9
- Settings → Account Settings → Cryptography
- S/MIME → Select "OpenSMIME"
- K-9 and OpenSMIME are now connected
