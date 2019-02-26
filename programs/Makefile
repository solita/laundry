NPROCS=$(shell getconf _NPROCESSORS_ONLN || echo 1)
OUT=/opt/laundry

install: $(OUT)/bin/nsjail $(OUT)/bin/pdf2jpeg $(OUT)/bin/gs $(OUT)/bin/pdf2txt $(OUT)/bin/pdf2pdfa $(OUT)/bin/checksum $(OUT)/sbin/firejail $(OUT)/bin/access-test $(OUT)/bin/sandbox-access-test.sh $(OUT)/bin/clean-caches $(OUT)/bin/lein $(OUT)/bin/convert # docker-libre-image

deps: nsjail-deps
	apt-get -y install libpng-dev libjpeg-dev build-essential clang wget unzip curl

nsjail-deps:
	apt-get -y install pkg-config bison flex libprotobuf-dev protbuf-compiler

## nsjail

nsjail:
	git clone https://github.com/google/nsjail.git
	cd nsjail && git checkout 2.8

nsjail/nsjail: nsjail
	cd nsjail && make

$(OUT)/bin/nsjail: nsjail/nsjail
	mkdir -p $(OUT)/bin
	cp -v nsjail/nsjail $(OUT)/bin


$(OUT)/bin/asan: deps /usr/bin/clang
	mkdir -p $(OUT)/bin
	echo '#!/bin/bash' > $(OUT)/bin/asan
	echo 'exec /usr/bin/clang -fsanitize=address -fstack-protector-strong -Wformat -Werror=format-security "$$@"' >> $(OUT)/bin/asan
	chmod +x $(OUT)/bin/asan

$(OUT)/bin/asan++: $(OUT)/bin/asan
	cat $(OUT)/bin/asan | sed -e 's/clang/clang++/' > $(OUT)/bin/asan++
	chmod +x $(OUT)/bin/asan++

$(OUT)/bin/pdf2jpeg: pdf2jpeg
	mkdir -p $(OUT)/bin
	cp pdf2jpeg $(OUT)/bin

$(OUT)/bin/clean-caches: clean-caches
	mkdir -p $(OUT)/bin
	cp clean-caches $(OUT)/bin

$(OUT)/bin/access-test: access-test
	mkdir -p $(OUT)/bin
	cp access-test $(OUT)/bin

$(OUT)/bin/sandbox-access-test.sh: sandbox-access-test.sh
	mkdir -p $(OUT)/bin
	cp sandbox-access-test.sh $(OUT)/bin

$(OUT)/bin/pdf2pdfa: pdf2pdfa
	mkdir -p $(OUT)/bin
	cp pdf2pdfa $(OUT)/bin

$(OUT)/bin/pdf2txt: pdf2txt
	mkdir -p $(OUT)/bin
	cp pdf2txt $(OUT)/bin

$(OUT)/bin/checksum: checksum
	mkdir -p $(OUT)/bin
	cp checksum $(OUT)/bin

tmp/firejail: 
	mkdir -p tmp
	cd tmp && git clone https://github.com/netblue30/firejail.git && cd firejail && git checkout 8a898ef19a40d34098218a165558e71817b0f006 # 0.9.56-LTS-release

tmp/firejail/src/firejail: tmp/firejail
	cd tmp/firejail && ./configure --prefix=$(OUT) && make

$(OUT)/sbin/firejail: tmp/firejail/src/firejail
	cd tmp/firejail && make install

$(OUT)/bin/gs:
	cd .. && build-scripts/ghostscript.bash
	cd ../build-data/src/ghostscript-9.26 && make install

$(OUT)/bin/lein:
	curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > $(OUT)/bin/lein
	chmod +x $(OUT)/bin/lein

$(OUT)/bin/convert: tmp/ImageMagick $(OUT)/bin/asan $(OUT)/bin/asan++
	cd tmp/ImageMagick && LD_LIBRARY_PATH=$(OUT)/lib64 CC=$(OUT)/bin/asan CXX=$(OUT)/bin/asan++ ./configure --prefix=$(OUT) && make -j$(NPROCS) && make install

tmp/ImageMagick:
	# we trust in https for this one, since the content varies
	mkdir -p tmp
	-rm -rf tmp/ImageMagick-*
	wget -O tmp/imagemagick.tar.gz https://github.com/ImageMagick/ImageMagick/archive/7.0.8-14.tar.gz
	sha256sum tmp/imagemagick.tar.gz | grep 0814f5599805ac7dfd6f46eefc7ea58c0a40dec6eb16b0a7ee0d499d3348e2c6
	cd tmp && tar -zxf imagemagick.tar.gz
	cd tmp && ln -s ImageMagick-* ImageMagick



clean:
	rm -rf tmp

docker-libre-image:
	docker build -t libreconv - < Dockerfile.libreoffice
