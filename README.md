# laundry

`laundry` converts user-supplied possibly dangerous files to more static and safer versions. Use it to reduce the risks of malware spreading via files supplied by external users or systems. The conversions are done with an up-to-date toolchain in a hardened stateless sandbox.

Antivirus products can mitigate the risks of malware, but they are imperfect. They mostly work against mass malware and have their own attack surfaces. `laundry` provides optional antivirus scans with [ClamAV](https://www.clamav.net/) open-source antivirus engine for additional level of security.

## Features

`laundry` provides an HTTP API for the conversions below.

| Input  | Output | Uses                                        | Purpose |
|--------|--------|---------------------------------------------|---------|
| doc(x) | pdf    | [LibreOffice](https://www.libreoffice.org/) | Removes any embedded macros etc and turns .doc(x) to portable PDF which can be e.g. embedded in HTML. |
| jpeg   | jpeg   | [Independent JPEG Group libjpeg](https://ijg.org/)     | Strip away all metadata and extraneous bytes, keep only pixel-by-pixel color data. Conversion performed with intermediate [PPM](https://en.wikipedia.org/wiki/Netpbm) format. |
| pdf    | pdf/a  | [Ghostscript](https://www.ghostscript.com/) | Clean up a PDF with conversion to [PDF/A](https://en.wikipedia.org/wiki/PDF/A) for archival purposes. Beware the potentially large file sizes. |
| pdf    | jpeg   | [Ghostscript](https://www.ghostscript.com/) | Converts the first page to jpeg for thumbnails or previews. |
| pdf    | text   | [Ghostscript](https://www.ghostscript.com/) | Extract plain text from a PDF. Does **not** perform [OCR](https://en.wikipedia.org/wiki/Optical_character_recognition). |
| png    | png    | [Netpbm](https://netpbm.sourceforge.net/)     | Strip away all metadata and extraneous bytes, keep only pixel-by-pixel color data. Conversion performed with intermediate [PPM](https://en.wikipedia.org/wiki/Netpbm) format. |
| xls(x) | pdf    | [LibreOffice](https://www.libreoffice.org/) |  Removes any embedded macros etc and turns .xls(x) to portable PDF which can be e.g. embedded in HTML. |

The `laundry` HTTP server provides an REST API and online tool to try out the conversions and antivirus scans directly from the browser. Optional API-key-based authorization is available.

Conversions are performed in single-use disposable Docker containers. The containers are secured, and their runtime is [gVisor](https://gvisor.dev/) `runsc`. It provides an additional layer of isolation for the containers.

Antivirus scan is exposed as an HTTP API. It takes in one file and the response tells whether there were any viruses in the file. The scans are performed with [ClamAV](https://www.clamav.net/) `clamdscan` from [their official Docker image](https://hub.docker.com/r/clamav/clamav). This container is not a single-use; instead it is kept alive for extended periods in order to keep the anti-virus signature database up-to-date.

## HTTP API documentation

The examples here use service address `http://192.168.123.123:8080` of local development environment. See [CONTRIBUTING.md](CONTRIBUTING.md) for instructions how to set it up.

Use the HTTP API in asynchronous manner; The provided endpoints can be slow. Processing a large file might take tens of seconds.

Each operation requires potentially hundreds of mebibytes of memory. Limit the amount of concurrent requests according to your server constraints.

### GET `/alive`

Endpoint for healthchecks. Invoke it to check whether the service is up and running.

_Authorization:_ No authorization required.

_Example request:_

```
curl http://192.168.123.123:8080/alive
```

_Responses:_

- HTTP status 200 with response body `yes`.

### GET `/auth-test`

Endpoint for testing your API KEY authorization without any actual operation.

_Authorization:_ Optional HTTP Basic authentication with user name `laundry-api` and your api-key as password. Authorization is required when the server is launched with `-k` or `--api-key-file` option.

_Example request:_

```
curl -u "laundry-api:abcd1234" http://192.168.123.123:8080/auth-test
```

_Responses:_

- HTTP status 200 when authorization is successful or when the server is running without authorization.
- HTTP status 401 for failed authorization with response body `access denied`.

### POST `/antivirus/scan`

Scans the attached file with ClamAV and indicates whether there were any viruses detected. The request must be `multipart/form-data` and the file in a part named `file`.

_Authorization:_ Optional HTTP Basic authentication as documented in [GET `/auth-test`](#get-auth-test).

_Example request:_

```
curl -F file=@input.xxx http://192.168.123.123:8080/antivirus/scan
```

_Responses:_

- HTTP status 200 when the file was clean and no viruses were detected.
- HTTP status 400 when viruses were detected! See response body for detailed response from `clamdscan`. It includes the virus name.
- HTTP status 401 for failed authorization. See [GET `/auth-test`](#get-auth-test) for details.
- HTTP status 500 when the scan can not be performed. See response body for detailed error message.

_Example response when virus detected:_

```
HTTP/1.1 400 Bad Request
Content-Type: text/plain;charset=utf-8

Viruses found! stream: Win.Test.EICAR_HDB-1 FOUND

----------- SCAN SUMMARY -----------
Infected files: 1
Time: 0.006 sec (0 m 0 s)
Start Date: 2022:10:19 07:22:16
End Date:   2022:10:19 07:22:16
```

### POST `/docx/docx2pdf`

Converts the provided `.doc` or `.docx` to a PDF. The request must be `multipart/form-data` and the file in a part named `file`.

_Authorization:_ Optional HTTP Basic authentication as documented in [GET `/auth-test`](#get-auth-test).

_Example request:_

```
curl -F file=@input.docx --output result.pdf http://192.168.123.123:8080/docx/docx2pdf
```

_Responses:_

- HTTP status 200 when the conversion succeeded. The `content-type` is `application/pdf` and the PDF is transferred in response body.
- HTTP status 401 for failed authorization. See [GET `/auth-test`](#get-auth-test) for details.
- HTTP status 500 when conversion failed. See server logs for details.

### POST `/xlsx/xlsx2pdf`

Converts the provided `.xls` or `.xlsx` to a PDF. The request must be `multipart/form-data` and the file in a part named `file`.

_Authorization:_ Optional HTTP Basic authentication as documented in [GET `/auth-test`](#get-auth-test).

_Example request:_

```
curl -F file=@input.xlsx --output result.pdf http://192.168.123.123:8080/xlsx/xlsx2pdf
```

_Responses:_

- HTTP status 200 when the conversion succeeded. The `content-type` is `application/pdf` and the PDF is transferred in response body.
- HTTP status 401 for failed authorization. See [GET `/auth-test`](#get-auth-test) for details.
- HTTP status 500 when conversion failed. See server logs for details.

### POST `/image/png2png`

Cleans up the provided `.png` keeping only pixel-by-pixel color data. The request must be `multipart/form-data` and the file in a part named `file`.

_Authorization:_ Optional HTTP Basic authentication as documented in [GET `/auth-test`](#get-auth-test).

_Example request:_

```
curl -F file=@input.png --output result.png http://192.168.123.123:8080/image/png2png
```

_Responses:_

- HTTP status 200 when the conversion succeeded. The `content-type` is `image/png` and the image is transferred in response body.
- HTTP status 401 for failed authorization. See [GET `/auth-test`](#get-auth-test) for details.
- HTTP status 500 when cleanup failed. See server logs for details.

### POST `/image/jpeg2jpeg`

Cleans up the provided `.jpg` or `.jpeg` keeping only pixel-by-pixel color data. The request must be `multipart/form-data` and the file in a part named `file`.

_Authorization:_ Optional HTTP Basic authentication as documented in [GET `/auth-test`](#get-auth-test).

_Example request:_

```
curl -F file=@input.jpeg --output result.jpeg http://192.168.123.123:8080/image/jpeg2jpeg
```

_Responses:_

- HTTP status 200 when the conversion succeeded. The `content-type` is `image/jpeg` and the image is transferred in response body.
- HTTP status 401 for failed authorization. See [GET `/auth-test`](#get-auth-test) for details.
- HTTP status 500 when cleanup failed. See server logs for details.

### POST `/pdf/pdf-preview`

Converts the first page of the PDF to jpeg. The request must be `multipart/form-data` and the file in a part named `file`.

_Authorization:_ Optional HTTP Basic authentication as documented in [GET `/auth-test`](#get-auth-test).

_Example request:_

```
curl -F file=@input.pdf --output result.jpeg http://192.168.123.123:8080/pdf/pdf-preview
```

_Responses:_

- HTTP status 200 when the conversion succeeded. The `content-type` is `image/jpeg` and the image is transferred in response body.
- HTTP status 401 for failed authorization. See [GET `/auth-test`](#get-auth-test) for details.
- HTTP status 500 when conversion failed. See server logs or response body for details.

### POST `/pdf/pdf2txt`

Extracts the contents of PDF to plain text. The request must be `multipart/form-data` and the file in a part named `file`.

_Authorization:_ Optional HTTP Basic authentication as documented in [GET `/auth-test`](#get-auth-test).

_Example request:_

```
curl -F file=@input.pdf --output result.txt http://192.168.123.123:8080/pdf/pdf2txt
```

_Responses:_

- HTTP status 200 when the extraction succeeded. The `content-type` is `text/plain` and the text is transferred in response body.
- HTTP status 401 for failed authorization. See [GET `/auth-test`](#get-auth-test) for details.
- HTTP status 500 when extraction failed. See server logs or response body for details.

### POST `/pdf/pdf2pdfa`

Converts the PDF to safer PDF/A, which is often used for archival purposes. This removes embedded scripts etc, but might also convert custom fonts to images. Thus the result might contain text as images, have large file sizes and be slow to open. The request must be `multipart/form-data` and the file in a part named `file`.

_Authorization:_ Optional HTTP Basic authentication as documented in [GET `/auth-test`](#get-auth-test).

_Example request:_

```
curl -F file=@input.pdf --output result.pdf http://192.168.123.123:8080/pdf/pdf2pdfa
```

_Responses:_

- HTTP status 200 when the conversion succeeded. The `content-type` is `application/pdf` and the PDF is transferred in response body.
- HTTP status 401 for failed authorization. See [GET `/auth-test`](#get-auth-test) for details.
- HTTP status 500 when conversion failed. See server logs or response body for details.

## Installation instructions

Three kinds of installation methods are presented in the following subsections. 

- We recommend to start by doing a [Temporary installation for demo purposes](#temporary-installation-for-demo-purposes) so that you can integrate the `laundry` to your systems and processes. 
- The [Production installation with Docker and gVisor runsc](#production-installation-with-docker-and-gvisor-runsc) outlines the procedures for a common installation on a dedicated server or vm.
- You can optionally continue to harden the setup with instructions given in [Customized production installations](#customized-production-installations)

### Temporary installation for demo purposes

**System requirements:** Linux or Mac & Docker

This installation method gives you the option to try out the laundry, integrate it into your systems or just to play around with it. This temporary installation method is **not suitable for production use** as it lacks sandboxing.

```sh
git clone https://github.com/solita/laundry.git
./laundry/docker-demo/build-and-run.sh
```

The script builds the necessary docker images including a temporary `laundry-demo`. It starts docker containers for the `laundry` HTTP server and for the ClamAV. The Docker host socket is exposed to the container, so that the `laundry-demo` can create temporary sibling containers for each conversion. `gVisor runsc` runtime is **not used** in the demo installation.

Default port is `8080`. The port can be given as parameter to the script

    ./docker-demo/build-and-run.bash -p 7777

See the script output for random api-key and the HTTP API address. Exit the demo with `docker stop laundry-demo`.

**Note:** Windows Subsystem for Linux users should be able to use the provided scripts. This has been tested on WSL version 2.

**Note:** macOS users might need to edit the script to run the `laundry-demo` container with `--user=root`, because the Docker socket has `root:root` ownership in the container.

**Note:** The demo configures Docker to expose this port to the internet and may open the host firewall for it.

### Production installation with Docker and gVisor runsc

**System requirements:** Linux with Docker, gVisor runsc and Java SDK

Install the prerequisites:

 1. Docker: https://docs.docker.com/engine/install/
 2. gVisor runsc: https://gvisor.dev/docs/user_guide/install/, https://gvisor.dev/docs/user_guide/production/ and https://gvisor.dev/docs/architecture_guide/platforms/
 3. Java SDK should be version 11 or newer: https://adoptium.net/temurin/releases/
 
**Note:** We recommend running [Docker Bench for Security](https://github.com/docker/docker-bench-security) before proceeding with the installation. It checks your Docker installation for common security-related best practices.

Download [a release](https://github.com/solita/laundry/releases) and lets and install laundry as systemd service. The following example assumes that:

- Current user is `laundry`
- The user `laundry` has privileges to run `docker`
- Current directory is `/home/laundry`
- All the [assets of a release](https://github.com/solita/laundry/releases) have been downloaded to `/home/laundry`
- The HTTP API should be run in port `8080`
- A random API KEY should be generated and used for authorization

```sh
# extract HTTP server and conversion programs
tar -xf release-*.tar.gz

# load the docker images
find -maxdepth 1 -name "docker-image-*-.tar.gz" -exec docker load --input {} \;

# generate a random API key
tr -dc 'a-zA-Z0-9' < /dev/urandom | head -c 32 >> /home/laundry/api-key.txt

# ClamAV container (must have the name `laundry-clamav`)
docker run \ 
    -d \
    --name laundry-clamav \
    --memory 4g \
    --ipc=private \
    --runtime=runsc \
    --cap-drop=ALL \
    clamav/clamav:latest

# systemd service
sudo tee /etc/systemd/system/laundry.service <<EOF
[Unit]
Description=Laundry services

[Service]
ExecStart=/usr/bin/java -jar /home/laundry/laundry/target/default+uberjar/laundry.jar -p 8080 --api-key-file /home/laundry/api-key.txt
User=laundry
Group=laundry
Type=simple
KillMode=process
Restart=always
WorkingDirectory=/laundry/home

[Install]
WantedBy=multi-user.target
EOF

# enable and start laundry
sudo systemctl enable laundry.service
sudo systemctl start laundry.service

# verify installation is running
curl -w "\n" http://localhost:8080/alive

# verify request without api key is rejected
curl -I http://localhost:8080/auth-test

# verify request with api key succeeds
curl -I -u "laundry-api:$(cat /home/laundry/api-key.txt)" http://localhost:8080/auth-test
```

### Customized production installations

 The `laundry` [production installation instructions ](#production-installation-with-docker-and-gvisor-runsc) set up ClamAV to run in a container. You can customize it to suit your environment. Please refer to the official [ClamAV docker documentation](https://docs.clamav.net/manual/Installing/Docker.html) to see different options.

You can install and run a customized `laundry` with alternative sandboxing, such as [nsjail](https://github.com/google/nsjail). The scripts in `programs/` will be executed by the `laundry` HTTP API, thus you have the option to customize their behaviour; Clone the repository and edit the contents of `programs/` to match your needs.

You could also run `laundry` without Docker; Check the `docker-build/` Dockerfiles for dependencies of `programs/` scripts. Install them or customized versions of them into the host. Clone the repository and edit the `programs/` to invoke those directly without Docker.

And instead of using `docker load` you might want to build the necessary docker images by yourself. A script and Dockerfiles for that purpose are included with the release. Check `docker-build/` folder from the [release-VERSIONNUMBER.tar.gz](https://github.com/solita/laundry/releases) asset.

## Development, releasing and contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)
