Tool to bulk upload users to the Studio instance.

#### Building:
`gradlew fatJar`

### Import
#### Running:
`java -jar ./build/libs/upload-users-1.0.jar {properties_file_path} {csv_file_path}`

#### Properties file content example:
```properties
url.base=http://localhost:18080/cxstudio/v1/api
login=admin@clarabridge.com
password=*********
threads=2
force.flag=0
extended=false
```

#### CSV file content:
Last 3 columns are only required, if `extended` property is set to `true`.

```csv
First Name,Last Name,Email Address,Password,License type,Master Account,Group Name,Unique ID,Custom Field
Some First Name,Some Second Name,some.email7@clarabridge.com,Password1!,1,1,Alert subscribed group,345,Custom Value
...
```

### Generation
#### Running:
`java -jar ./build/libs/generate-users-1.0.jar {properties_file_path} {output_csv_path}`

#### Properties file content example:
```properties
count=10
masterAccountId=123
#License type id: Designer(1), Studio(2), Viewer(4)
licenseId=4
password=Password1!
emailDomain=clarabridge_test.com
#Comma separated list of first names for generation
firstNames=John,Jane
#Comma separated list of last name for generation
lastNames=Doe
#Comma separated list of groups
groups=Group 1,Group 2
#Comma separated list of custom user property
customValues=
#Random seed to be used for lists above, empty - random every time
randomSeed=
```