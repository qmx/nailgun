@echo off
ng  C:NGCache put echo1 Hello1 30000
sleep 25
ng  C:NGCache put echo2 Hello2 40000
sleep 25
ng  C:NGCache put echo3 Hello3 120000
sleep 25
ng  C:NGCache put echo4 Hello4 10000
echo Fetching echo5
ng C:NGCache get echo5
echo Fetching echo4
ng C:NGCache get echo1
ng C:NGCache get echo2
ng C:NGCache get echo3
ng C:NGCache get echo4
ng C:NGCache get echo1
ng C:NGCache get echo2
ng C:NGCache get echo3
ng C:NGCache get echo4
ng C:NGCache get echo1
ng C:NGCache get echo2
ng C:NGCache get echo3
ng C:NGCache get echo4
ng C:NGCache get echo1
ng C:NGCache get echo2
ng C:NGCache get echo3
ng C:NGCache get echo4
