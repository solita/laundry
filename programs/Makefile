NPROCS=$(shell getconf _NPROCESSORS_ONLN || echo 1)
OUT=/opt/laundry

install: $(OUT)/bin/nsjail $(OUT)/bin/pdf2jpeg $(OUT)/bin/gs $(OUT)/bin/pdf2txt $(OUT)/bin/pdf2pdfa $(OUT)/bin/docx2pdf $(OUT)/bin/jpeg2jpeg $(OUT)/bin/png2png $(OUT)/bin/checksum $(OUT)/bin/clean-caches $(OUT)/bin/lein $(OUT)/bin/convert

deps: nsjail-deps
	apt-get update && apt-get -y upgrade && apt-get -y install libpng-dev libjpeg-dev build-essential clang wget unzip curl

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

$(OUT)/bin/asan: /usr/bin/clang
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


$(OUT)/bin/checksum: checksum
	mkdir -p $(OUT)/bin
	cp checksum $(OUT)/bin

$(OUT)/bin/gs:
	cd .. && build-scripts/ghostscript.bash
	cd ../build-data/src/ghostscript-9.50 && make install

$(OUT)/bin/lein:
	curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > $(OUT)/bin/lein
	chmod +x $(OUT)/bin/lein

$(OUT)/bin/convert: tmp/ImageMagick $(OUT)/bin/asan $(OUT)/bin/asan++
	cd tmp/ImageMagick && LD_LIBRARY_PATH=$(OUT)/lib64 CC=$(OUT)/bin/asan CXX=$(OUT)/bin/asan++ ./configure --prefix=$(OUT) --enable-hdri=no && make -j$(NPROCS) && make install

tmp/ImageMagick:
	mkdir -p tmp
	-rm -rf tmp/ImageMagick-*
	wget -O tmp/imagemagick.tar.gz https://github.com/ImageMagick/ImageMagick/archive/7.0.9-7.tar.gz
	sha256sum tmp/imagemagick.tar.gz | grep 601bb6efb64c7d28038cc255b0d4331a08690e6146252b21bf202b0fa33b68f6
	cd tmp && tar -zxf imagemagick.tar.gz
	cd tmp && ln -s ImageMagick-* ImageMagick

/usr/local/bin/runsc:
	wget -O /usr/local/bin/runsc.new https://storage.googleapis.com/gvisor/releases/nightly/2019-06-26/runsc
	sha512sum /usr/local/bin/runsc.new | grep 639666d962a23560bf14f9db4ce275478e96d0b7db5a8f37b49c675d2558a6a9ca6d42d80b25c099c2850c3e464cf962a459d30d80744a00bd0f13ff26b33381
	chmod +x /usr/local/bin/runsc.new
	mv  /usr/local/bin/runsc.new  /usr/local/bin/runsc

clean:
	rm -rf tmp

