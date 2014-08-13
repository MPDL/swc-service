swc-service
===========

### Intro
The web service generate visual represenations of SWC files    


### Installation

1. Install Maven [download](http://maven.apache.org/download.cgi)
2. Install Tomcat [download](http://maven.apache.org/download.cgi)
3. `git clone https://github.com/MPDL/swc-service`
5. Compile the service: Go into `swc-service directory`, run `mvn clean install`
6. Copy `swc.war` in Tomcat `webapp` directory
7. Create File `swc-service.properties` and add following parameters 
8. 
9. 
10. and put it into Tomcat `conf` directory
8. Edit `magick.properties` with Property `imagemagick.convert.bin = /path/to/convert` (for instance `/usr/bin/convert`)
9. Start Tomcat
10. Service runs under `http://localhost:8080/magick`

### Usage
The magick service implemtents the method `GET` and `POST`. The parameters are:
- **url** (Mandotory for `GET`): the url of the file to be transformed
- **size**: As defined by imagemagick [resize](http://www.imagemagick.org/script/command-line-options.php#resize)
- **crop**:As defined by imagemagick [crop](http://www.imagemagick.org/script/command-line-options.php#crop)
- **format**: The format in which the file shhould be returned (for instance png, jpg, etc.)
- **priority** (size|crop): The method (size or crop) which is processed first (only relevant when resize and crop are both used)


