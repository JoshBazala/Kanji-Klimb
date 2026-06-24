@echo off
"C:\\Program Files\\Android\\Android Studio\\jbr\\bin\\java" ^
  --enable-native-access ^
  ALL-UNNAMED ^
  --class-path ^
  "C:\\Users\\joshb\\.gradle\\caches\\modules-2\\files-2.1\\com.google.prefab\\cli\\2.1.0\\aa32fec809c44fa531f01dcfb739b5b3304d3050\\cli-2.1.0-all.jar" ^
  com.google.prefab.cli.AppKt ^
  --build-system ^
  cmake ^
  --platform ^
  android ^
  --abi ^
  x86 ^
  --os-version ^
  35 ^
  --stl ^
  c++_static ^
  --ndk-version ^
  28 ^
  --output ^
  "C:\\Users\\joshb\\AppData\\Local\\Temp\\agp-prefab-staging6034392159120294811\\staged-cli-output" ^
  "C:\\Users\\joshb\\.gradle\\caches\\9.4.1\\transforms\\4695cd87027816cfff909f5465337390\\transformed\\games-activity-4.0.0\\prefab"
