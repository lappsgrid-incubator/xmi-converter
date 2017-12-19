# XMI/LIF Conversion

This is a simple proof of concept conversion program between XMI and LIF.  

**NOTE** This example does not handle arbitrary XMI, just the example XMI files provided that use annotations from the `http:///gov/hhs/fda/srs/annotation/vaers.ecore` namespace.

## Building

This project requires Java 8 and Maven 3.x

```bash
mvn package
```

This will generate the executable jar file `target/convert.jar` that can convert files between XMI and LIF.  The program accepts a single parameter, which is the path to the file to be converted.  If the file name ends with *.xml* it will be converted to LIF, and if the file name ends with *.lif* it will be converted to XMI.  

```bash
java -jar target/convert.jar src/test/resources/data/outputClinicalETHER.xml
``` 

The `round-trip.sh` script will use the `convert.jar` program to first convert all of the example XML files to LIF, and then convert the LIF files back to XMI.

## Design Notes

This example uses a `SAXParser` to read the XMI files and a `DOMBuilder` to generate the XMI files. A real implementation would use a UIMA type system and related classes to read/write the XMI files.  However, this example does show what the generated LIF files would look like.

