# Shut-Up!

Gradle Dependencies:

```java
dependencies {
    compile 'com.android.support:multidex:1.0.0'
    compile 'com.github.citux:datetimepicker:0.2.0'
    compile 'com.github.flavienlaurent.datetimepicker:library:0.0.2'
    compile 'com.takisoft.fix:preference-v7:25.0.1.0'
    compile 'com.android.support:percent:25.0.1'
    compile 'com.android.support:preference-v14:25.0.1'
    compile 'com.takisoft.fix:preference-v7:25.0.1.0'
    compile 'com.android.support:support-v4:25.0.1'
    compile 'com.android.support:support-v13:25.0.1'
    compile 'com.android.support:appcompat-v7:25.0.1'
    compile 'com.android.support:design:25.0.1'
    compile 'com.android.support:gridlayout-v7:25.0.1'
    compile 'com.android.support:support-vector-drawable:25.0.1'
    compile 'com.android.support:support-annotations:25.0.1'
    compile 'com.android.support:appcompat-v7:25.0.+'
    compile 'com.google.android.gms:play-services:10.0.1'
    compile 'com.android.support:mediarouter-v7:25.0.1'
    compile 'com.android.support:mediarouter-v7:25.0.1'
    compile fileTree(dir: 'libs', include: ['*.jar'])
}

ant.importBuild 'assets.xml'
preBuild.dependsOn(list, checksum)
clean.dependsOn(clean_assets)
```

This uses the CMU Sphnix Library for voice recognition @ http://cmusphinx.sourceforge.net/
