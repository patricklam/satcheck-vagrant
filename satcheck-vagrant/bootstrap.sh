#!/usr/bin/env bash

case $(id -u) in
    0) 
        add-apt-repository "deb http://llvm.org/apt/trusty/ llvm-toolchain-$(lsb_release -s -c) main"
        wget -O - http://llvm.org/apt/llvm-snapshot.gpg.key|sudo apt-key add -
        apt-get update
        apt-get install -y subversion cmake g++ git zlib1g-dev libedit-dev clang-3.8 libclang-3.8-dev
        sudo -u vagrant -i $0
        ;;
    *)
        #ssh-keyscan -t rsa github.com
        #echo "github.com ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAq2A7hRGmdnm9tUDbO9IDSwBK6TbQa+PXYPCPy6rbTrTtw7PHkccKrpp0yVhp5HdEIcKr6pLlVDBfOLX9QUsyCOV0wzfjIJNlGEYsdlLJizHhbn2mUjvSAHQqZETYP81eFzLQNnPHt4EVVUh7VfDESU84KezmD5QlWpXLmvU31/yMf+Se8xhHTvKSCZIFImWwoG6mbUoWf9nzpIoaSjB+weqqUUmpaaasXVal72J+UX2B+2RPW3RcT0eOzQgqlJL3RKrTJvdsjE3JEAvGq3lGHSZXy28G3skua2SmVi/w4yCE6gbODqnTWlg7+wC604ydGXA8VJiS5ap43JXiUFFAaQ==" >> ~/.ssh/known_hosts
        #echo "patricklam.ca ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAsieWLTnd6Jt4lxF4XMoC8woAD4Gvscl5e547365E0Ue3GyzhSy3LJJdVH5LQn++xvHvdqCc8zcxiZkLrXKAQaIR8HzEUy3rr2l/UnroTBgfMB+/JJiLc7+6+YlDaovnyBQAIx99Ca6WKN4gHE74pRLRWmUESdh6eH+zACK1PqG4CN2A3HJBeIv9kIqSwIpHuUrB7axv3ght8kwmMEPzXYBVFkDkjyUFMKD3oF5iM7JkJuzbk1E8c5AGNY0Hjdmhcc0aBRJ6pIhP6uzn6oUl/NsJ3NLPOmzuyq5pp9CLpxJjs9TSvDLHQ2AnHyiwzm9CXHEekkHRjWNYsZoUpdwGLZw==" >> ~/.ssh/known_hosts
        mkdir -p hacking
        cd hacking

        # fetch and build satcheck
        rm -rf sat
        rm -rf model-checker2 model-checker2-tso

        # preferred option: fetch from synced folder
        cp /vagrant_data/sat.tar.gz .
        cp /vagrant_data/mc2.tar.gz .
        tar xzvf sat.tar.gz
        tar xzvf mc2.tar.gz

        # option: fetch from git
        # git clone git://patricklam.ca/sat.git
        # git clone git://patricklam.ca/model-checker2.git

        # option: fetch over wget
        # wget http://patricklam.ca/files/sat.tar.gz
        # tar xzvf sat.tar.gz

        # wget http://patricklam.ca/files/mc2.tar.gz
        # tar xzvf mc2.tar.gz

        # build glucose-syrup; make; copy for TSO version
        (cd sat/glucose-syrup/incremental; make)

        (cd model-checker2; cp ../sat/glucose-syrup/incremental/glucose sat_solver)
        (cd model-checker2; make)

        # path.sh for benchmark runs
        CLANG_INCLUDE_DIR=/usr/lib/llvm-3.8/lib/clang/3.8.0/include/
        echo MC2DIR=~/hacking/model-checker2/ > path.sh
        echo "export PATH=\$MC2DIR:\$PATH" >> path.sh
        echo "export CLANG_INCLUDE_DIR=$CLANG_INCLUDE_DIR" >> path.sh
        cp path.sh model-checker2/benchmarks/satcheck
        cp path.sh model-checker2/benchmarks/satcheck-precompiled

        # build clang
        sed -i "/^LLVM_VERSION/s|.*|LLVM_VERSION := -3.8|" model-checker2/clang/Makefile
        (cd model-checker2/clang; make)

        # ensure CLANG_INCLUDE_DIR is also correct in bin/annotate script
        sed -i "/^CLANG_INCLUDE_DIR/s|.*|CLANG_INCLUDE_DIR=$CLANG_INCLUDE_DIR|" model-checker2/bin/annotate
        ;;
esac
