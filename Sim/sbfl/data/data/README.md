This directory contains:

- some data-massaging scripts
- 2 files full of scores
- a blacklist of artificial faults that shouldn't be analyzed
- a specification of how many passing/failing tests each fault has (`num_tests.csv`), and script to automatically compute it (`num_tests.sh`).



Scoring Files
-------------

- `scores_artificial_vs_real.csv.gz`

    Contains scores for prior-work techniques, run on real and artificial faults:

    + 4 debugging scenarios: first, last, mean, median
    + 7 FLTs: Tarantula, Ochiai, Barinel, Op2, DStar, Metallaxis, MUSE
    + all real/artificial faults with 32h-timeout killmaps, provided there is a corresponding artificial/real fault with a 32h-timeout killmaps.

    The *Bug* column in `scores_artificial_vs_real.csv.gz` indicates whether a fault
    for a given *Project* is real or artificial:

    + real faults have Bug IDs < 1000
    + artificial faults have Bug IDs > 100000

    Note that the Bug ID of an artificial fault encodes the Bug ID of the
    corresponding real fault: `artificial Bug ID = real Bug ID * 100000 + num`,
    where num is the number of the artificial fault for a given real fault. For
    example, the following Bug IDs represent one real fault (1) and three
    corresponding artificial faults (100001-100003):

    + 1
    + 100001
    + 100002
    + 100003


- `scores_real_exploration.csv.gz`

    Contains scores for all the techniques mentioned in the paper, run on real faults:

    + 4 debugging scenarios: first, last, mean, median
    + all FLTs: the whole design space, plus our experimental techniques
    + all real faults for which we could generate killmaps, imposing no timeout


Blacklist
---------

This blacklist file `blacklist.csv` lists the artificial faults that have any of the following problems:

1. it won't compile
2. the tests time out
3. no tests detect it
4. a test-class that fails rather than just a test

Ideally, these problems would be detected by Major when it's generating artificial faults, and the problematic faults would be discarded; but Major runs the test suites all at once, while we run them one-at-a-time, so due to test-dependency issues, some of these problems slip by.

(At the time of writing, issues 1-3 are indicated by an empty or one-line `trigger_tests` file in D4J; issue 4 is indicated by a line in the `trigger_tests` file naming a whole *class* that fails rather than a method.)
