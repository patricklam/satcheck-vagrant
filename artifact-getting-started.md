SATCheck Artifact Evaluation: Getting Started
=============================================

We've packaged SATCheck using Vagrant 1.6.5
(https://www.vagrantup.com), and it should be fairly easy to get
SATCheck running. In particular, the provided `Vagrantfile` and
`bootstrap.sh` provisioning script will produce a VM image with SATCheck
downloaded and mostly built. Equivalently, you can use the provided
VM image. 

We've set up the Vagrant configuration to create a VM that uses 16GB
of memory. You can change that by changing the line in the
`Vagrantfile` that contains `"--memory", "16384"`. Note that you may
not be able to run all problem sizes with less memory; see the
step-by-step guide for instructions on changing the problem sizes.

This getting started file will describe the remaining steps after
running `vagrant up`. After following the steps, you'll generate the
graphs that are in the paper (modulo hardware differences, of course).

Provided VM image
-----------------

If you use the provided VM image instead of Vagrant, configure
VirtualBox with at least 2 CPUs and at least 16 GB of RAM.  We have
already downloaded LLVM and Clang 3.8 and compiled our frontend
against them in the provided VM, so you can skip those steps.

To log into the VM, use the username/password pair vagrant/vagrant.
The Vagrant command `vagrant ssh` takes care of authentication for you
and just logs you into the Vagrant VM.

Quick tips: Vagrant
-------------------
If you aren't familiar with how Vagrant works, the two commands you need
to know are `vagrant up`, which provisions a virtual machine (it downloads
an image and runs a provisioning script), and `vagrant ssh`, which connects
to your virtual machine. Run both of them in the same directory as the
`Vagrantfile` and `bootstrap.sh` files.

Directory layout
----------------
The Vagrant VM is set up with `/home/vagrant` as the home directory.
You'll find a copy of SATCheck below there in
`/home/vagrant/hacking/model-checker2`.

The benchmarks live in `~/hacking/model-checker2/benchmarks/satcheck`.
(There is also a version which has already been run through the
frontend at `satcheck-precompiled`.) You will find the five benchmarks
in that directory: `dekker`, `linuxlock`, `linuxrwlock`,
`msqueueoffset`, and `seqlock`.

You can run each benchmark individually using the `bench.sh` and
`benchtso.sh` files in the benchmark directory. (They are identical
except that they choose different input sizes; benchmark
configurations, including input sizes, are in `benchmark-config.sh` in
each of the individual benchmark directories). The `runsat` and
`runtsosat` scripts run all of the benchmarks and collect the data.

Running SATCheck with the Clang front-end
----------------------------------------- 
If you have Clang 3.8 and its development libraries installed (as in
the Vagrant VM), you should be able to build and run the SATCheck
frontend. The `bootstrap.sh` Vagrant provisioning file creates a
known-working installation on top of an Ubuntu trusty64 VM image.

To re-build the SATCheck frontend (we do this by default for you,
so you only need to re-build if you change the frontend):

1. `cd hacking/model-checker2/clang`
2. `make`

We've configured the Makefile in the `model-checker2/clang` directory
so that the frontend will build on Clang 3.8. If you're using a
different version of Clang, change the Makefile's `LLVM_VERSION`
setting accordingly.

You can start all of the benchmarks from
`~/hacking/model-checker2/benchmarks/satcheck`.  In that directory,
the `runsat` and `runtsosat` scripts insert instrumentation in the
`*_unannotated.c` file, creating `*.c` files, compiling them with
SATCheck, and running them.

To instrument (with the Clang frontend) and individually run the
benchmarks in SC mode, do the following:
1. `cd hacking/model-checker2/benchmarks/satcheck`.
2. Go to the desired benchmark's sub-directory.
3. Run `./bench.sh`.

To run in TSO mode, run `benchtso.sh` instead of `bench.sh`.

Running the benchmarks
----------------------
Once you bring up the VM, here are the steps to run the pre-compiled benchmarks.
You can modify the `runsat` and `runtsosat` scripts to point to `satcheck-precompiled`
to run the benchmarks without processing them through the front-end.

1. `cd hacking/model-checker2/benchmarks`
2a. `./runsat`
2b. `./runtsosat`

This simply runs all of the SATCheck benchmarks and records the
results in "log" files in each subdirectory. The following commands
will concatenate log files, useful in the next step.

3. `find ~/hacking/model-checker2/benchmarks/satcheck-precompiled -name log | xargs cat > results_sat`
4. `find ~/hacking/model-checker2/benchmarks/satcheck-precompiled -name log | xargs cat > results_tsosat`

The benchmarks will run in the VM (unless they run out of resources,
e.g. if you allocate less than 16G of RAM). However, one would expect
the performance to be different from that on bare-metal. The
Vagrantfile and provisioning script provide enough information for you
to recreate our setup on a bare-metal hardware configuration.

Generating graphs
-----------------
Graph generation is not in-VM. Instead, use the files in the `graphs`
subdirectory of the artifact submission. To generate the graphs, copy
the results files produced above into the `graphs` subdirectory,
compile the parser.java file and run the provided `runall` script in
the `graphs` directory. That script runs the `parser.java` program
with the SATCheck results produced above, and produces a set of
gnuplot `.dat` and `.cmd` files which produce the plots in the paper.
The script also runs gnuplot.

We have included results from the related tools on our hardware
(Ubuntu Linux 14.04, Xeon E3-1246 v3 CPUs, 32GB of RAM). The graphs
probably won't be to scale if you compare runs from different
hardware, but time constraints prevented us from packaging the related
tools.

Tips
----
Here are some things that you might want to know. 

Specifying options for the Clang frontend is a bit unwieldy.  The
script `bin/annotate` invokes the Clang frontend with the proper
arguments on the specified input and outputs the annotated code to
stdout. It also works on `.h` files. See the bench.sh files for
invocation examples for the Clang frontend.
