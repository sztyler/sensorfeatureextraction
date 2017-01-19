# Sensor Feature Factory (Java)
Sensor feature extraction for inertial sensor data. This application allows to compute several different common features for any window size.

## Releases
Download: [latest build](https://github.com/sztyler/sensorfeatureextraction/releases/tag/Build190117) (JAR, Build190117)

## Usage
```java
FeatureFactory ff = new FeatureFactory();
fe.start();

File fileAcc = new File("data/example/acc_walking_shin.csv");
try (BufferedReader br = new BufferedReader(new FileReader(fileAcc))) {
    String line;
    while ((line = br.readLine()) != null) {
        String[] fragments = line.split(",");
        long     timestamp = Long.parseLong(fragments[1]);
        float[]  values    = new float[]{Float.parseFloat(fragments[2]), Float.parseFloat(fragments[3]), Float.parseFloat(fragments[4])};
        
        ff.addRecord(SensorType.ACCELEROMETER, timestamp, values, "walking", "shin", "mall", "shopping");
    }
}

NavigableMap<Long, Window> result = new TreeMap<>();
do {
    result.putAll(ff.getWindows());
    // wait until it is done or do something
} while (!ff.isIdle());
result.putAll(ff.getWindows());

for (Window window : result.values()) {    // print all windows
    System.out.println(window);
}

String csvFile = ff.getWindowsAsCSV();
System.out.println(csvFile);

String arffFile = ff.getWindowsAsARFF(0);  // 0 = walking, 1 = shin, 2 = mall, 3 = shopping
System.out.println(arffFile);
```
[Please also consider the complete example](https://github.com/sztyler/sensorfeatureextraction/blob/master/src/de/unima/sensor/features/example/UsageExample.java)

## Documentation
**Documentation (JavaDoc) is coming soon** . If you have any issues, feel free to [contact me](http://sensor.informatik.uni-mannheim.de)
