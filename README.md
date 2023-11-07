# Authentication service

## Description
This is authentication service can be used for clients' identification for any other service. It uses JWT token-based authorization. 

There are to APIs:
* Internal API based on gRPC for internal services
* External API based on HTTP for customers
* External health API based on HTTP

You can get the gRPC API reference from `src/main/proto/user/v1/UserService.proto`.

For the HTTP APIs you can send GET request to the `/auth/api/docs` endpoint.

## Security
For internal API service uses TLS. You should prepare server and client certificates to use it.

You can configure a path to certificates in `application.yaml` file. By default, it uses `certs/` folder.

External API is open and can be used without authentication.

## Configuration
1. Prepare certificates. For demo, you can use self-signed certificates:
   ```
   #CA
   openssl genrsa 2048 > ca.key
   openssl req -new -x509 -nodes -days 365000 -key ca.key -out ca.crt
   
   #Server
   openssl req -newkey rsa:2048 -subj "//CN=localhost" -nodes -keyout server.key -out server-req.pem
   openssl x509 -req -days 365000 -in server-req.pem -out server.crt -CA ca.crt -CAkey ca.key
   
   #Client
   openssl req -newkey rsa:2048 -nodes -days 365000 -keyout client-web-service.key -out client-web-service-req.pem
   openssl x509 -req -days 365000 -set_serial 01 -in client-web-service-req.pem -out client-web-service.crt -CA ca.crt -CAkey ca.key
   ```
2. Put all certificates to the `certs/` folder.
3. Add all client certs to the `certs/trusted-clients.crt.collection`:
   ```
   cat client*.crt > trusted-clients.crt.collection
   ```
4. If you need, do some changes in configuration file `application.yaml`.
5. Generate secret tokens (`JWT_SECRET_ACCESS`, `JWT_SECRET_REFRESH`). You can use `utils.SecretsGenerator.main()` method for this.

## Build
1. Previously build and install the [grpc interface lib](https://github.com/Serguncheouss/auth-service-grpc-interface)
2. Build the project:
   ```
   mvn clean
   mvn package
   ```

## Run
### Direct run
1. You can set environment variables `GRPC_PORT`, `HTTP_PORT`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET_ACCESS`, `JWT_SECRET_REFRESH` and run:
   ```
   java -jar .\target\auth-service-1.0.jar
   ```
   or pass it to command and run:
   ```
   java -DGRPC_PORT=8090 -DHTTP_PORT=8080 \
   -DDB_PORT=3306 -DDB_NAME=db_name -DDB_USER=db_user -DDB_PASSWORD=db_password \
   -DJWT_SECRET_ACCESS=secret_access -DJWT_SECRET_REFRESH=secret_refresh \
   -jar .\target\auth-service-1.0.0.jar
   ```
### Docker
1. You can create a `.env` file and run by docker-compose:

   `.env` file:
   ```
   DB_NAME=db_name
   DB_USER=db_user
   DB_PASSWORD=db_password
   DB_ROOT_PASSWORD=db_root_password
   DB_PORT=3306
   HTTP_PORT=8080
   GRPC_PORT=8090
   JWT_SECRET_ACCESS=secret_access
   JWT_SECRET_REFRESH=secret_refresh
   ```
   command:
   ```
   sudo docker-compose --env-file .env up --build -d
   ```
2. Or you can run `start.sh` with variables:
   ```
   sudo ./start.sh master \
   DB_NAME=db_name DB_USER=db_user DB_PASSWORD=db_password DB_ROOT_PASSWORD=db_root_password DB_PORT=3306 \
   HTTP_PORT=8090 GRPC_PORT=8100 \
   JWT_SECRET_ACCESS=secret_access JWT_SECRET_REFRESH=secret_refresh
   ```
   * The first parameter `master` is the commit from the repository.

   It will pull changes from the repository checkout to `COMMIT` build the code and create docker containers and run it.