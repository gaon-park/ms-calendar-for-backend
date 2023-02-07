# ms-calendar-for-backend

- web calendar rest api server

  1. Member base
  2. Personal/shared schedule
  3. Member search
  4. Based follower/following relationship

# swagger

- local 

  1. run springboot
  2. http://localhost:8080/swagger-ui/index.html

- dev
  
   1. http://ms-hero.kr/swagger-ui/index.html
      1. need admin basic(nginx config)

# how to deploy

```
$ ssh 34.64.59.204
$ cd ms-calendar-for-backend
$ git pull origin master

## If the server instance is newly started (for dabatase server)
$ sudo docker-compose up -d 
## access db
## and external host connection blocking set
$ mysql -uroot -p -h127.0.0.1
mysql$ use mysql;
mysql$ delete
       from user
       where host = "%";

$ ./gradlew clean build -x test
$ java -jar -Dspring.profiles.active=.{env} build/libs/hero-for-backend-0.0.1-SNAPSHOT.jar 

## background start 
## $ nohup java -jar -Dspring.profiles.active=.{env} build/libs/hero-for-backend-0.0.1-SNAPSHOT.jar &
```
