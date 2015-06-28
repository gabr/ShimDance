filename convention:
shimmer_[id]_mode_[mode]_sampling_[rate]_sensors_[sensors]_arange_[acc_range]_grange_[gyr_range]_title_[title].csv/dat

[id]: shimmer id, see back side of sensor node
[mode]:
- sc: shimmer connect
- sdc: sd log (calibrated data)
- sdr: sd log (raw data)
[rate]:
0: 10.2
1: 51.2
2: 102.4
3: 128
4: 170.7
5: 204.8
6: 256
7: 512
8: 1024
[sensors]: chosen sensors
- acc
- gyr
- accgyr
- ecg
- accecg
[acc_range]: accelerometer range 
0: 1.5 g
1: 2 g
2: 4 g
3: 6 g
[gyr_range]: gyroscope range
0: 500 °/s
1: 2000 °/s
[title]: title of file content (e.g. name of exercise)