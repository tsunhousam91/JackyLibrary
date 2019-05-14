# JackyLibrary
A useful library, you can use this library to do the following things:<br/>

1.FileUtils:<br/>
read files, write files, copy files, delete files, and you can get the filePath you want conveniencly.

2.LogUtils:<br/>
Help you log easily, it will generates appropriate TAG automatically, and you can turn on the function "save log", if you do, it will help you save the logs to file, each log file will be named by the date, e.g. "20190514_log.txt", the log files will save in "Android/data/[packageName]/logs/" by default, if your device doesn't have the outer storage, then it will use the inner space, that is "data/data/[packageName]/logs/"

3.PreferenceUtils:<br/>
Help you get the SharedPreference easily.<br/>

4.TimeUtils:<br/>
Help you to get the customFormat you want, you need to pass the timeStamp you want to convert, or call getNowDateFormat(), it will use the current timeStamp,it will update more functions afterward.<br/>

5.StringUtils:<br/>
Help you handle some String problem,it will update more functions afterward.<br/>

# To use this library, you need to add the following to your gradle.
allprojects {<br/>
&nbsp;&nbsp;		repositories {<br/>
&nbsp;&nbsp;&nbsp;&nbsp;			...<br/>
&nbsp;&nbsp;&nbsp;&nbsp;			maven { url 'https://jitpack.io' }<br/>
&nbsp;&nbsp;  }<br/>
}<br/>

//TAG might be like this: 1.3.0 or other version you want.<br/>
dependencies {<br/>
&nbsp;&nbsp;   implementation 'com.github.tsunhousam91:JackyLibrary:Tag'<br/>
}<br/>
