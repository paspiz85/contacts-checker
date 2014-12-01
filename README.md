contacts-checker
================
Execute some checks and some fix on your Google Contact's book.
```
mvn compile exec:java -Dexec.mainClass="contacts.Test" \
-Dgoogle.username="email" \
-Dgoogle.password="password" \
-Dvcards.input="contacts.vcf" \
-Dvcards.output="vcf" \
-Dfacebook.calendar="file.ics" \
-Dfacebook.token="fbtoken"
```
