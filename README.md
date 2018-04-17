# Android-App-SVM-
A sample SVM library for Android.
https://github.com/yctung/AndroidLibSVM
* Train a model by picking a user selected training file from the data folder (containing both walking and stationary) to build a model using only acceleration. The format of the file as follows. 
0th row {first name, last name, mobile, email, gender, age}
1st row {timestamp, lat, long, accelx, accely, accelz, label}
2nd row onwards contain values as per the format in the 1st row
* Read a user selected test file from the data folder containing the same information and apply the learned model for prediction. 
* Output a file in the data folder containing the prediction in the following format 
0th row {first name, last name, mobile, email, gender, age}
1st row {timestamp, lat, long, accelx, accely, accelz, label, prediction}
2nd row onwards contain values as per the format in the 1st row 
