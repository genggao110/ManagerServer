# ManagerServer

<p align="center">
  <a href="http://geomodeling.njnu.edu.cn/" target="_blank" >
    <img width="180" src="http://opengmsteam.com/images/logo2.png" alt="logo" style="background:black">
  </a>
</p>

<p aligin="center">
  <a href="http://geomodeling.njnu.edu.cn/" target="_blank">
    <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="152" height="20">
  <linearGradient id="b" x2="0" y2="100%">
    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
    <stop offset="1" stop-opacity=".1"/>
  </linearGradient>
  <clipPath id="a">
    <rect width="152" height="20" rx="3" fill="#fff"/>
  </clipPath>
  <g clip-path="url(#a)">
    <path fill="#555" d="M0 0h80v20H0z"/>
    <path fill="#007ec6" d="M80 0h80v20H80z"/>
    <path fill="url(#b)" d="M0 0h152v20H0z"/>
  </g>
  <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="110"> 
    <text x="400" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)" textLength="500">OpenGMS</text>
    <text x="400" y="140" transform="scale(.1)" textLength="500">OpenGMS</text>
    <text x="1150" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)" textLength="420">Official</text>
    <text x="1150" y="140" transform="scale(.1)" textLength="420">Official</text>
  </g> 
</svg>
  </a>
</p>

### Introduction
Manager Server(based on Portal) for geographical modeling, which is mainly used to help Portal Website find model service and Invoke model resources. It communicate with the task server to finish most works.
 
### Framework

#### Workflow

![Overview](https://ws1.sinaimg.cn/large/005CDUpdly1g1aon03ysfj35ah3nnb29.jpg)

### Database

table `TaskNode` : for Task Server

| Field | Type | Introduction |
| ----- |----- | ------------ |
|  _id  | ObjectID | OID for task node |
| name | String | Task Server name, usually the server machine name |
| host  | String | Task Server IP address |
| port | String |Port for Task Server |
| system | String | Task Server system type: windows or Linux |
| createDate | Date | Task Server register time |
| geoInfo | Object | Task Server basic information: city,countryCode,latitude,countryName,region,longtitude |
| register | Boolean | Task Server register status |


### API

#### _Task Server Register_

* URL : _/taskNode_
* Method : POST
* Form : 
```json
{
    "name":"localhost",
    "host":"169.235.24.133",
    "port":"8060",
    "system":"Linux"
}
```
* Response :
```json
{
    "code": 0,
    "msg": "成功",
    "data": {
        "id": "5c938cd1fedf28528c37d875",
        "name": "localhost",
        "host": "169.235.24.133",
        "port": "8060",
        "system": "Linux",
        "createDate": 1553173712139,
        "geoInfo": {
            "city": "Riverside",
            "countryCode": "US",
            "latitude": "33.9533",
            "countryName": "United States",
            "region": "CA",
            "longitude": "-117.3962"
        },
        "register": "njgis"
    }
}
```
#### _Get Suitable Task for Invoking model by pid_

* URL : _/taskNode/getServiceTask/{pid}_
* Method : GET
* Response ：
```json
{
    "code": 0,
    "msg": "成功",
    "data": {
        "id": "5c8b61ab1932413d2c24d6d1",
        "host": "172.21.212.119",
        "port": "8061",
        "status": true,
        "running": 0
    }
}
```

#### _Create Task for Preparing the model invoke_

* URL : _/computableModel/createTask_
* Method : POST
* Form :
```json
{
	"ip":"172.21.212.119",
	"port":8061,
	"pid":"faa3fa6554e822154862800961a99e51",
	"username":"wangming"
}
```
* Response:
```json
{
    "code": 0,
    "msg": "成功",
    "data": {
        "ip": "172.21.212.119",
        "port": 8061,
        "pid": "faa3fa6554e822154862800961a99e51",
        "dxServer": {
            "ip": "172.21.212.155",
            "port": 8062
        },
        "inputData": {
            "items": [],
            "count": 0
        },
        "outputData": {
            "items": [],
            "count": 0
        },
        "username": "wangming",
        "tid": "",
        "status": null
    }
}
```

#### _Upload model input data_

* URL : _/computableModel/uploadData_
* Method : POST
* FormData:

| Key | Value |
| ----- |----- | 
|  host  | 172.21.212.155 |
|  port  | 8062 |
|  tag   | input |
|  file  | data11.fds |

* Response :
```json
{
    "code": 0,
    "msg": "成功",
    "data": {
        "statename": null,
        "event": null,
        "url": "http://172.21.212.155:8062/data/5c8ba7ebd91d7b0a5c78d161?pwd=RktOcVpOalV6TVRNMU16QTJOVFkwTXpFek1ESmtNelF6TnpNeU16VXlaRE14TXpFMk5UTTVNbVF6T1RNMU16QXpNekprTmpZMk5EWTBNemd6T1RNeU16ZzJNall5TXpFek1EWXlnZ2NRZQ==",
        "tag": "input"
    }
}
```

#### _Subscribe the task or Invoke the model_

* URL : _/computableModel/invoke_
* Method : POST
* Form :
```json
{
	"ip":"172.21.212.119",
	"port":8061,
	"pid":"faa3fa6554e822154862800961a99e51",
	"username":"wangming",
	"inputs":[{
		"statename":"RUNSTATE",
		"event":"LOADDATASET",
		"url":"http://172.21.212.155:8062/data/5c8ba7ebd91d7b0a5c78d161?pwd=RGhBcnNOalV6TVRNMU16QTJOVFkwTXpFek1ESmtNelF6TnpNeU16VXlaRE14TXpFMk5UTTVNbVF6T1RNMU16QXpNekprTmpZMk5EWTBNemd6T1RNeU16ZzJNall5TXpFek1EWXlMcmpnZQ==",
		"tag":"data11"
	}]
}
```
* Response : 
```json
{
    "code": 0,
    "msg": "成功",
    "data": {
        "tid": "5c93513d64380f2a8c8d8b86"
    }
}
```

### Install Requirement

- Java JDK 1.8+
- Maven

### Usage

Construction ...


### Contact Us

Ming Wang (<wangminggis@163.com>) 



