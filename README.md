contacts-checker
================
mvn compile exec:java -Dexec.mainClass="contacts.Test" \
-Dgoogle.username="paspiz85@gmail.com" \
-Dgoogle.password="password" \
-Dvcards.input="contacts.vcf" \
-Dvcards.output="vcf" \
-Dfacebook.calendar="u1560751739.ics" \
-Dfacebook.token="fbtoken"
