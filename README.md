contacts-checker
================
Execute some checks and some fixes on your Google Contact's book.
Run with:
```
mvn compile exec:java -Dexec.mainClass="contacts.Test" \
-Dgoogle.username="email" \
-Dgoogle.password="password" \
-Dvcards.process="true" \
-Dvcards.input="contacts.vcf" \
-Dvcards.output="vcf" \
-Dfacebook.calendar="file.ics" \
-Dfacebook.token="fbtoken"
```
