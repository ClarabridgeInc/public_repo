Tool to bulk upload users to the Studio instance.

Building:
`gradlew fatJar`

Running:
`java -jar ./build/libs/upload-user-jar-1.0.jar {properties_file_path} {csv_file_path}`

Properties file content example:
```properties
url.base=http://localhost:18080/cxstudio/api/v1
login=pavel.dzunovich@clarabridge.com
password=*********
threads=2
force.flag=0
extended=false
```

CSV file content:
```csv
First Name,Last Name,Email Address,Password,License type,Master Account,Group Name,Unique ID,Custom Field
Some First Name,Some Second Name,some.email7@clarabridge.com,Password1!,1,1,Alert subscribed group,345,Custom Value
...
```