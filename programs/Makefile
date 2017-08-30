GMP=gmp-4.3.2
ISL=isl-0.15
MPC=mpc-0.8.1
MPFR=mpfr-2.4.2
GCCINFRA=ftp://gcc.gnu.org/pub/gcc/infrastructure/
THREADS=4
OUT=/opt/laundry

everything: $(OUT)/bin/pdf2png $(OUT)/bin/gs $(OUT)/bin/pdf2txt $(OUT)/bin/pdf2pdfa $(OUT)/bin/checksum $(OUT)/sbin/firejail $(OUT)/bin/access-test $(OUT)/bin/sandbox-access-test.sh

$(OUT)/bin/asan: $(OUT)/bin/gcc
	mkdir -p $(OUT)/bin
	echo '#!/bin/bash' > $(OUT)/bin/asan
	echo 'exec $(OUT)/bin/gcc -fsanitize=address "$$@"' >> $(OUT)/bin/asan
	chmod +x $(OUT)/bin/asan

$(OUT)/bin/asan++: $(OUT)/bin/asan
	cat $(OUT)/bin/asan | sed -e 's/gcc/g++/' > $(OUT)/bin/asan++
	chmod +x $(OUT)/bin/asan++

$(OUT)/bin/pdf2png: pdf2png
	mkdir -p $(OUT)/bin
	cp pdf2png $(OUT)/bin

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
	
$(OUT)/bin/gcc: tmp/gcc-6.3.0.tar.gz
	# check dependencies
	curl --version
	bzip2 --version
	zip --version
	unzip -v
	# compile
	mkdir -p tmp
	cd tmp && tar -zxf gcc-6.3.0.tar.gz
	-rm -rfv tmp/gcc-6.3.0/gmp* tmp/gcc-6.3.0/isl* tmp/gcc-6.3.0/mpc* tmp/gcc-6.3.0/mpfr* 
	cd tmp/gcc-6.3.0 && curl $(GCCINFRA)/$(MPFR).tar.bz2 | tee mpfr.tar.bz2 | sha256sum | grep c7e75a08a8d49d2082e4caee1591a05d11b9d5627514e678f02d66a124bcf2ba && tar -jxvf mpfr.tar.bz2 && mv $(MPFR) mpfr
	cd tmp/gcc-6.3.0 && curl $(GCCINFRA)/$(ISL).tar.bz2 | tee isl.tar.bz2 | sha256sum | grep 8ceebbf4d9a81afa2b4449113cee4b7cb14a687d7a549a963deb5e2a41458b6b && tar -jxvf isl.tar.bz2 && mv $(ISL) isl
	cd tmp/gcc-6.3.0 && curl $(GCCINFRA)/$(GMP).tar.bz2 | tee gmp.tar.bz2 | sha256sum | grep 936162c0312886c21581002b79932829aa048cfaf9937c6265aeaa14f1cd1775 && tar -jxvf gmp.tar.bz2 && mv $(GMP) gmp
	cd tmp/gcc-6.3.0 && curl $(GCCINFRA)/$(MPC).tar.gz | tee mpc.tar.gz | sha256sum | grep e664603757251fd8a352848276497a4c79b7f8b21fd8aedd5cc0598a38fee3e4 && tar -zxvf mpc.tar.gz && mv $(MPC) mpc
	cd tmp/gcc-6.3.0 && ./configure --disable-multilib --prefix=$(OUT) && nice -15 make -j$(THREADS) && make install
	$(OUT)/bin/gcc --version | grep 6\.3\.0

tmp/firejail: 
	mkdir -p tmp
	cd tmp && git clone https://github.com/netblue30/firejail.git
	
tmp/firejail/src/firejail: tmp/firejail
	cd tmp/firejail && ./configure --prefix=$(OUT) && make

$(OUT)/sbin/firejail: tmp/firejail/src/firejail
	cd tmp/firejail && make install

tmp/gcc-6.3.0.tar.gz:
	mkdir -p tmp
	curl http://ftp.gnu.org/pub/gnu/gcc/gcc-6.3.0/gcc-6.3.0.tar.gz > tmp/gcc.tar.gz
	sha256sum tmp/gcc.tar.gz | grep 02f9302a559fa2251595ca0bc1e937219eff2995a3802d7b31676fec2402beb4
	mv tmp/gcc.tar.gz tmp/gcc-6.3.0.tar.gz

tmp/ghostscript.tar.gz:
	mkdir -p tmp
	curl -L "https://github.com/ArtifexSoftware/ghostpdl-downloads/releases/download/gs921/ghostscript-9.21.tar.gz" > tmp/gs 
	sha256sum tmp/gs | grep 02bceadbc4dddeb6f2eec9c8b1623d945d355ca11b8b4df035332b217d58ce85
	mv tmp/gs tmp/ghostscript.tar.gz

$(OUT)/bin/gs: tmp/ghostscript.tar.gz $(OUT)/bin/asan $(OUT)/bin/asan++
	cd tmp && tar -zxf ghostscript.tar.gz
	cd tmp/ghostscript-9.21 && CC=$(OUT)/bin/asan CXX=$(OUT)/bin/asan++ LD_LIBRARY_PATH=$(OUT)/lib:$(OUT)/lib64  ./configure --prefix=$(OUT) 
	cd tmp/ghostscript-9.21 && CC=$(OUT)/bin/asan CXX=$(OUT)/bin/asan++ LD_LIBRARY_PATH=$(OUT)/lib:$(OUT)/lib64  make -j4 install
	
clean:
	rm -rf tmp
