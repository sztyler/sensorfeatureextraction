# Sensor Feature Extraction (Java)
Sensor feature extraction for accelerometer sensor data. This application allows to compute several different common features for any window size.

## Releases
Download: [latest build](https://github.com/sztyler/sensorfeatureextraction/releases/tag/Build300916) (JAR, Build300916)

## Usage
```java
FE fe = new FE(Sensor.ACCELERATION);
fe.start();

File file = new File("data/example/acc_data.csv");
try (BufferedReader br = new BufferedReader(new FileReader(file))) {
  String line;
  while ((line = br.readLine()) != null) {
    String[] fragments = line.split(",");
    long     timestamp = Long.parseLong(fragments[1]);
    float[]  values    = new float[]{Float.parseFloat(fragments[2]), Float.parseFloat(fragments[3]), Float.parseFloat(fragments[4])};

    fe.addAccelerationData(timestamp, values, Action.DEVICEPOSITIONS.SHIN, Action.HUMANPOSTURES.WALKING, "mall", "shopping");
  }
}

do {
  Thread.sleep(1000);
} while (!fe.isIdle());

String arffFile = fe.getWindowsAsARFF(Action.TYPE.HUMANPOSTURES);
System.out.println(arffFile);
```
[Please also consider the complete example](https://github.com/sztyler/sensorfeatureextraction/blob/master/src/de/unima/ar/collector/features/example/UsageExample.java)

## Documentation
**Documentation (JavaDoc) is coming soon** . If you have any issues, feel free to [contact me](http://sensor.informatik.uni-mannheim.de)
