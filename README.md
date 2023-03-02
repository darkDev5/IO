# IO
Gives you some nice and basic futures to work with files and folders easily.

## Usage
In this example we create instance of File class and fetch some attributes from system.

```java
var thisFile = new File("D:\\drivers\\nvidia_driver.exe");

System.out.println(thisFile.getAttribute("name"));
System.out.println(thisFile.getAttribute("owner"));
System.out.println(thisFile.getAttribute("createDate"));
```
