Tool to bulk upload users to the Studio instance.

Building:
`gradlew fatJar`

Running:
`java -jar ./build/libs/upload-user-jar-1.0.jar {properties_file_path} {csv_file_path}`

Properties file content example:
```properties
url=http://10.142.237.91/cxstudio/api/v1/users/%s/account
login=admin@clarabridge.com
password=******
csv.file={csv_file_path}
threads=2
force.flag=0
```

CSV file content:
```csv
First Name,Last Name,Email Address,Password,License type,Master Account
Some First Name,Some Second Name,some.email@email.com,Password,license_type_id,master_account_id
```