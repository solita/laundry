NPROCS=$(shell getconf _NPROCESSORS_ONLN || echo 1)
OUT=/opt/laundry

install: $(OUT)/bin/nsjail $(OUT)/bin/pdf2jpeg $(OUT)/bin/pdf2txt $(OUT)/bin/pdf2pdfa $(OUT)/bin/docx2pdf $(OUT)/bin/jpeg2jpeg $(OUT)/bin/png2png $(OUT)/bin/lein

deps: nsjail-deps
	apt-get update && apt-get -y upgrade && apt-get -y install wget curl

nsjail-deps:
	apt-get -y install pkg-config bison flex libprotobuf-dev protobuf-compiler

## nsjail

nsjail:
	git clone https://github.com/google/nsjail.git
	cd nsjail && git checkout 2.8

nsjail/nsjail: nsjail
	cd nsjail && make

$(OUT)/bin/nsjail: nsjail/nsjail
	mkdir -p $(OUT)/bin
	cp -v nsjail/nsjail $(OUT)/bin

$(OUT)/bin/pdf2jpeg: pdf2jpeg
	mkdir -p $(OUT)/bin
	cp pdf2jpeg $(OUT)/bin

$(OUT)/bin/pdf2pdfa: pdf2pdfa
	mkdir -p $(OUT)/bin
	cp pdf2pdfa $(OUT)/bin

$(OUT)/bin/docx2pdf: docx2pdf
	mkdir -p $(OUT)/bin
	cp docx2pdf $(OUT)/bin

$(OUT)/bin/pdf2txt: pdf2txt
	mkdir -p $(OUT)/bin
	cp pdf2txt $(OUT)/bin

$(OUT)/bin/jpeg2jpeg: jpeg2jpeg
	mkdir -p $(OUT)/bin
	cp jpeg2jpeg $(OUT)/bin

$(OUT)/bin/png2png: png2png
	mkdir -p $(OUT)/bin
	cp png2png $(OUT)/bin


$(OUT)/bin/lein:
	curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > $(OUT)/bin/lein
	chmod +x $(OUT)/bin/lein

/usr/local/bin/runsc:
	wget -O /usr/local/bin/runsc.new https://storage.googleapis.com/gvisor/releases/nightly/2019-06-26/runsc
	sha512sum /usr/local/bin/runsc.new | grep 639666d962a23560bf14f9db4ce275478e96d0b7db5a8f37b49c675d2558a6a9ca6d42d80b25c099c2850c3e464cf962a459d30d80744a00bd0f13ff26b33381
	chmod +x /usr/local/bin/runsc.new
	mv  /usr/local/bin/runsc.new  /usr/local/bin/runsc

clean:
	rm -rf tmp

