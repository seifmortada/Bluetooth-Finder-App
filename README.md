# Bluetooth-Finder-App
This is part of my graduation project which is indoor positioning system by bluetooth 
we used 3 bluetooth beacons which are arduino nano and HM-10 Bluetooth module connected to them ,
Then the mobile application scans for the beacons and show the distance from each one by filtering the RSSI values by the help of bluetoothHelper Library
and using method to transform the RSSI values to distance ,
Then from the 3 values we get the current position of the device using the trilateration method ,
After we get the X Y values we send them to the ESP32 Which is the base beacon then it upload the values to the cloud (Firebase) for the admin 
After that we get the values and draw them on a map on the Desktop Application .
#There are screens of the app
![image](https://github.com/seifmortada/Bluetooth-Finder-App/assets/76921289/567bdb41-4ad0-4e5e-868e-955fc0b0ed13)
![image](https://github.com/seifmortada/Bluetooth-Finder-App/assets/76921289/e5da4c75-90b8-4934-9c08-4e01e08bf044)


#The project documentation
[Project Indoor Positioning System (1).pdf](https://github.com/seifmortada/Bluetooth-Finder-App/files/12064743/Project.Indoor.Positioning.System.1.pdf)
