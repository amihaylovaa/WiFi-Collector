**Wi-Fi Collector** is an Android mobile application that collects and stores locally data for the available wireless networks situated around identified location in GPS Exchange Format (GPX) file.
Locally stored data is later sent to remote database through [Web service](https://github.com/amihaylovaa/WiFi-Collector-Storage-Service).


When the app is launched the user can chоose between scanning (collecting) data for wireless networks or sending it to the remote database, if any data is present.

 ![Main](https://github.com/amihaylovaa/WiFi-Collector/blob/master/MainActivity.png)

# START SCANNING  
  1) Requests for turning GPS on and granting location permission to the app is sent to the user. If both are granted, foreground service is started where the main logic of the app is placed (see from 2) and below )
  2) Periodic location request is sent to Google Play Services API - Fused location API. New location is expected to be received in a range of 3-5 seconds with high accuracy of the result.
  3) New location is received.
  4) Wi-Fi scanning is triggered - results are received asynchronously.
  5) Data from 2) and 3) is stored in the GPX file.
  6) Steps 2), 3) and 4)  are repeated until the user stops collecting data.
  
 ![Scanning](https://github.com/amihaylovaa/WiFi-Collector/blob/master/ScanningActivity.png)
  
  After collecting is done, the user MUST send the data otherwise collecting will not be allowed. After the data is sent successfully, the user is free to collect data again.
  
# SEND DATA 
  1) Data is read from the GPX file and parsed to JSON format.
  2) HTTP request is created.
  3) TCP connection is established with remote server.
  4) Data is sent to the web server and stored in the database.
  5) HTTP response is received and Toast message is displayed to inform the user how the operation has finished.


* Several dialog fragments (dialog window wrapped in dialog) are shown to the user explaining why certain actions are required, for example turning GPS on.

* Unit tests are planned in future.
