contacts-checker
================
'''
mvn compile exec:java -Dexec.mainClass="contacts.Test" \
-Dgoogle.username="email" \
-Dgoogle.password="password" \
-Dvcards.input="contacts.vcf" \
-Dvcards.output="vcf" \
-Dfacebook.calendar="file.ics" \
-Dfacebook.token="fbtoken"
'''
