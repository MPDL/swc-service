swc-service
===========

### Intro
The REST web service generates visual represenations of [SWC files](http://research.mssm.edu/cnic/swc.html)    


### Installation

1. Install Maven [download](http://maven.apache.org/download.cgi)
2. Install Tomcat [download](http://maven.apache.org/download.cgi)
3. Install L-Measure [download](http://cng.gmu.edu/Lm/)
4. `git clone https://github.com/MPDL/swc-service`
5. Compile the service: go into `swc-service directory`, run `mvn clean install`
6. Copy `swc.war` in Tomcat `webapp` directory
7. Create file `swc-service.properties`, add following property in it
```
screenshot.service.url = base_url_to_screenshot_service/screenshot
lmeasure.bin = path_to_the_lmeasure_binary
```
 and put the file into Tomcat `conf` directory

8. Start Tomcat
9. Service runs under `http://localhost:8080/swc`

### Usage

Following REST commands are implemented:

##### **Path**: /api/view
- **Method**: POST
- **Media type**: multipart/form-data
- **Input fields**: 
  - field 1:
    - **name**: file1
    - **type**: file
    - **value**: locally selected SWC file
  - field 2:
    - **name**: portable
    - **type**: text
    - **value**: true|false(default)
- **Response**:
Delivers HTML view representation of the SWC file. 
 
##### **Path**: /api/view
- **Method**: POST
- **Media type**: application/x-www-form-urlencoded
- **Input fields**: 
  - field 1:
    - **name**: swc
    - **type**: text/texarea
    - **value**: SWC text
  - field 2:
    - **name**: portable
    - **type**: text
    - **value**: true|false(default)
- **Response**:
Delivers HTML view representation of the SWC text. 

##### **Path**: /api/view
- **Method**: GET
- **Media type**: application/x-www-form-urlencoded
- **Input fields**: 
  - field 1:
    - **name**: url
    - **type**: text
    - **value**: link to the SWC URL
  - field 2:
    - **name**: portable
    - **type**: text
    - **value**: true|false(default)
- **Response**:
returns HTML view representation of the SWC text referenced by the URL. 

**Note:** Setting of `portable="true"` generates html which can be used offline.

##### **Path**: /api/analyze
- **Method**: POST
- **Media type**: multipart/form-data
- **Input fields**: 
  - field 1:
    - **name**: file1
    - **type**: file
    - **value**: locally selected SWC file
  - field 2:
    - **name**: numberOfBins
    - **type**: number
    - **value**: [see L-Measure help](http://cng.gmu.edu/Lm/help/index.htm)
  - field 3:
    - **name**: typeOfBins
    - **type**: text
    - **value**: number|width [see L-Measure help](http://cng.gmu.edu/Lm/help/index.htm)
  - field 3:
    - **name**: query
    - **type**: text
    - **value**: The L-Measure functions (for instance -f0,0,0,10.0 ) [see L-Measure help](http://cng.gmu.edu/Lm/help/index.htm)
- **Response**:
Delivers JSON File with the L-Measure result. 
 
##### **Path**: /api/analyze
- **Method**: POST
- **Media type**: application/x-www-form-urlencoded
- **Input fields**: 
  - field 1:
    - **name**: swc
    - **type**: text/texarea
    - **value**: SWC text
   - field 2:
    - **name**: numberOfBins
    - **type**: number
    - **value**: [see L-Measure help](http://cng.gmu.edu/Lm/help/index.htm)
  - field 3:
    - **name**: typeOfBins
    - **type**: text
    - **value**: number|width [see L-Measure help](http://cng.gmu.edu/Lm/help/index.htm)
  - field 3:
    - **name**: query
    - **type**: text
    - **value**: The L-Measure functions (for instance -f0,0,0,10.0 ) [see L-Measure help](http://cng.gmu.edu/Lm/help/index.htm)
- **Response**:
Delivers JSON File with the L-Measure result. 

##### **Path**: /api/analyze
- **Method**: GET
- **Media type**: application/x-www-form-urlencoded
- **Input fields**: 
  - field 1:
    - **name**: url
    - **type**: text
    - **value**: link to the SWC URL
   - field 2:
    - **name**: numberOfBins
    - **type**: number
    - **value**: [see L-Measure help](http://cng.gmu.edu/Lm/help/index.htm)
  - field 3:
    - **name**: typeOfBins
    - **type**: text
    - **value**: number|width [see L-Measure help](http://cng.gmu.edu/Lm/help/index.htm)
  - field 3:
    - **name**: query
    - **type**: text
    - **value**: The L-Measure functions (for instance -f0,0,0,10.0 ) [see L-Measure help](http://cng.gmu.edu/Lm/help/index.htm)
- **Response**:
Delivers JSON File with the L-Measure result. 

##### **Path**: /api/thumb
- **Method**: POST
- **Media type**: multipart/form-data
- **Input fields**: 
  - field:
    - **name**: file1
    - **type**: file
    - **value**: locally selected SWC file
- **Response**:
returns PNG screenshot of the rendered SWC file. 
 
##### **Path**: /api/thumb
- **Method**: POST
- **Media type**: application/x-www-form-urlencoded
- **Input fields**: 
  - field:
    - **name**: swc
    - **type**: text/texarea
    - **value**: SWC text
- **Response**:
returns PNG screenshot of the rendered SWC text


##### **Path**: /api/thumb
- **Method**: GET
- **Media type**: application/x-www-form-urlencoded
- **Input fields**: 
  - field:
    - **name**: url
    - **type**: text
    - **value**: link to the SWC text 
- **Response**:
returns PNG screenshot of the renederd SWC text referenced by the URL. 


You can check `http://localhost:8080/swc/api/explain` page for swc-service usage examples.   