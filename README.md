# ManagerServer

<p align="center">
  <a href="http://geomodeling.njnu.edu.cn/" target="_blank" >
    <img width="180" src="http://opengmsteam.com/images/logo2.png" alt="logo" style="background:black">
  </a>
</p>

[![Travis (.org)](OpenGMS.svg)](http://geomodeling.njnu.edu.cn/)

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
    "code": 1,
    "msg": "suc",
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
    "code": 1,
    "msg": "suc",
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
    "code": 1,
    "msg": "suc",
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
    "code": 1,
    "msg": "suc",
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
    "code": 1,
    "msg": "suc",
    "data": {
        "tid": "5c935d1464380f2a8c8d8b8b"
    }
}
```

#### _Refresh the task Record by tid_

* URL : _/computableModel/refreshTaskRecord_
* Method: POST
* Form :
```json
{
	"ip":"172.21.212.119",
	"port":8061,
	"tid":"5c935d1464380f2a8c8d8b8b"
}
```
* Response :
```json
{
    "code": 1,
    "msg": "suc",
    "data": {
        "ip": "172.21.212.119",
        "port": 8061,
        "tid": "5c935d1464380f2a8c8d8b8b",
        "pid": "faa3fa6554e822154862800961a99e51",
        "status": 2,
        "inputs": null,
        "outputs": [
            {
                "statename": "RUNSTATE",
                "event": "RETURNDATASET",
                "url": "http://172.21.212.155:8062/geodata/SHtqOU1Nekl6TnpZMk5qRXpOVFkyTXpJek1ESmtNelEyTWpZeU5qVXlaRE14TXpFMk5UTTVNbVEyTVRZeE16WTJOREprTXpFek1UTTFNemN6TmpNeE5qSXpPVE0xTXprek1UWXpaeVYvRg==",
                "tag": "RUNSTATE-RETURNDATASET"
            }
        ]
    }
}
```
Tag: Task Status, including `Inited`(0), `Started`(1), `Finished`(2) and `Error`(-1).

#### _Get Suitable Task Server for Deploying the Model Package(Micro Service)_

* URL : _/taskNode/getTaskForMicroService_
* Method : GET
* Response : 
```json
{
    "code": 1,
    "msg": "suc",
    "data": {
        "id": "5c8b61ab1932413d2c24d6d1",
        "host": "172.21.212.119",
        "port": "8061",
        "status": true,
        "running": 0
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



