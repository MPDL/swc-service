swc-service
===========

### Intro
The REST web service generates visual represenations of SWC files    


### Installation

1. Install Maven [download](http://maven.apache.org/download.cgi)
2. Install Tomcat [download](http://maven.apache.org/download.cgi)
3. `git clone https://github.com/MPDL/swc-service`
4. Compile the service: go into `swc-service directory`, run `mvn clean install`
5. Copy `swc.war` in Tomcat `webapp` directory
6. Create file `swc-service.properties`, add following property in it

> `screenshot.service.url = base_url_to_screenshot_service/html-screenshot`

 and put the file into Tomcat `conf` directory

7. Start Tomcat
8. Service runs under `http://localhost:8080/swc`

### Usage

Following REST commands are implemented:
- **Method**: POST
- **Path**: /api/view
- **Request**: 
- **Media type**: multipart/form-data
- **Input fields*: 
-- name1: file1
-- type1: file
-- value1: locally selected SWC file
-- name2: portable
-- type2: text
-- value2: true|false
Response:
Delivers HTML view representation of the SWC file
 

----------------------

The magick service implemtents the method `GET` and `POST`. The parameters are:
- **url** (Mandotory for `GET`): the url of the file to be transformed
- **size**: As defined by imagemagick [resize](http://www.imagemagick.org/script/command-line-options.php#resize)
- **crop**:As defined by imagemagick [crop](http://www.imagemagick.org/script/command-line-options.php#crop)
- **format**: The format in which the file shhould be returned (for instance png, jpg, etc.)
- **priority** (size|crop): The method (size or crop) which is processed first (only relevant when resize and crop are both used)


