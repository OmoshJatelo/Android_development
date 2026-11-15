# Android Sensors Inventory

An Android application for hardware and software sensor inventory and capability analysis. This app scans sensors reported by the Android device, categorizes them, inspects detailed metadata (range, resolution, power, reporting mode), and safe-checks sensor availability.

## Features

- **Device Diagnostics & Summary**: Automatically detects device manufacturer, model, Android OS version, API level, and uses heuristics to detect physical devices vs. emulators.
- **Complete Sensor Inventory**: Queries `SensorManager.getSensorList(Sensor.TYPE_ALL)` to display all available sensors with full driver metadata.
- **Category Filtering**: Allows filtering the inventory list by categories:
  - **Motion** (Accelerometer, Gyroscope, Step Counter, Linear Acceleration, Rotation Vector, etc.)
  - **Position** (Magnetometer, Proximity, Geomagnetic Rotation, etc.)
  - **Environment** (Light, Pressure/Barometer, Ambient Temperature, Relative Humidity, etc.)
  - **Other** (Heart Rate, unlisted/custom types)
- **Detailed Metadata Inspection**: Tapping any sensor opens a capability dialog detailing:
  - Sensor Type & Category
  - Vendor & Version
  - Unit of Measurement
  - Maximum Range & Resolution
  - Power Consumption (mA)
  - Min/Max Delay ($\mu$s) & streaming frequency
  - Reporting Mode (Continuous, On Change, One Shot, Special Trigger)
  - FIFO event buffering counts (Reserved / Max)
  - Wake-up & Dynamic sensor capabilities
- **Single-Sensor Probe Check**: Test presence of specific sensor types via `getDefaultSensor()` to check if a feature is supported on the target hardware.
- **Test Mode (Empty Inventory Simulation)**: Toggle simulated empty state to test UI resilience and safe fallback handling when sensors are unavailable.
- **Rotation Resilience**: Preserves user selections (filter category and test mode state) across device orientation changes.

## Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 11 or higher
- Android SDK with API 36 installed (Minimum SDK 24 / Target SDK 36)

### Build & Run
To assemble the debug APK from command line:
```bash
./gradlew assembleDebug
```

To run local unit tests:
```bash
./gradlew testDebugUnitTest
```

## Project Structure

```
AndroidSensorsInventory/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smart/androidsensorsinventory/
│   │   │   │   ├── MainActivity.java     # Main UI launcher, filtering & dialog management
│   │   │   │   └── SensorFormat.java     # Metadata formatting, units, heuristics & calculations
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       │   ├── activity_main.xml
│   │   │       │   ├── header_sensor_inventory.xml
│   │   │       │   └── item_sensor_inventory.xml
│   │   │       └── values/
│   │   └── test/                         # Unit tests
│   └── build.gradle.kts                  # App module configuration
├── build.gradle.kts                      # Root Gradle configuration
└── settings.gradle.kts
```

## License
This project is licensed under the MIT License - see the LICENSE file for details.
