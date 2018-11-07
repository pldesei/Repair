#!/usr/bin/env perl
#
# This script parses the data file that contains the results for all artificial
# vs real faults. This data file contains results only for the best-case debugging scenario.
# For each data row for an artificial or real fault, the scripts outputs
# the results for all debugging scenarios (to STDOUT), by either duplicating the
# results for artificial faults or looking up the missing results for real
# faults in the data file that holds the exploration results.
#
# usage: ./get_all_real.pl <data file artificial vs. real> <data file real exploration>

use strict;
use warnings;

# Check arguments
$#ARGV==1 or die "usage: $0 <data file artificial vs. real> <data file all real>";
my $ART_VS_REAL = $ARGV[0];
my $ALL_REAL = $ARGV[1];

# Make sure the provided files exist
-e "$ART_VS_REAL" or die "$ART_VS_REAL file not found!";
-e "$ALL_REAL" or die "$ALL_REAL file not found!";

my %all_real = ();

# Cache the results for all real faults
open(IN, "<$ALL_REAL");
while(<IN>) {
    # Skip header
    /Project/ and next;
    my $line = $_;
    # Remove line ending (Dos or Unix)
    $line =~ s/\r?\n$//;
    my ($Project,$Bug,$TestSuite,$ScoringScheme,$Family,$Formula,$TotalDefn,$KillDefn,$HybridScheme,$AggregationDefn,$Score,$ScoreWRTLoadedClasses) = split /,/;
    my $key = "$Project,$Bug,$TestSuite,first,$Family,$Formula,$TotalDefn,$KillDefn,$HybridScheme,$AggregationDefn";
    unless (defined $all_real{$key}) {
        $all_real{$key} = {};
    }
    $all_real{$key}->{$ScoringScheme} = $line;
}
close(IN);

open(IN, "<$ART_VS_REAL");
my $header = <IN>;
print($header);
while(<IN>) {
    my $line = $_;
    # Remove line ending (Dos or Unix)
    $line =~ s/\r?\n$//;
    my ($Project,$Bug,$TestSuite,$ScoringScheme,$Family,$Formula,$TotalDefn,$KillDefn,$HybridScheme,$AggregationDefn,$Score,$ScoreWRTLoadedClasses) = split /,/;
    my $key = "$Project,$Bug,$TestSuite,$ScoringScheme,$Family,$Formula,$TotalDefn,$KillDefn,$HybridScheme,$AggregationDefn";
    # Simply duplicate the data rows for artificial faults -- the results for all scoring schemes are identical.
    if ($Bug > 1000) {
        print "$Project,$Bug,$TestSuite,first,$Family,$Formula,$TotalDefn,$KillDefn,$HybridScheme,$AggregationDefn,$Score,$ScoreWRTLoadedClasses";
        print "$Project,$Bug,$TestSuite,median,$Family,$Formula,$TotalDefn,$KillDefn,$HybridScheme,$AggregationDefn,$Score,$ScoreWRTLoadedClasses";
        print "$Project,$Bug,$TestSuite,last,$Family,$Formula,$TotalDefn,$KillDefn,$HybridScheme,$AggregationDefn,$Score,$ScoreWRTLoadedClasses";
        next;
    }
    # Sanity checks:
    # Does the real fault exist in both data files?
    # Does the data file (real exploration) contain results for all 4 scoring schemes?
    # Are the results or the scoring scheme first identical in both data files?
    #
    defined $all_real{$key} or die "Missing results for: $key";
    scalar(keys %{ $all_real{$key} } ) == 4 or die "Unexpected number of scoring schemes (" . scalar(keys %{ $all_real{$key} } ) . " != 4)for: $key";
    # We consistently use the results of the data file that includes all scoring schemes
    #
    #$line eq $all_real{$key}->{"first"} or warn "Inconsistent results for: $key\n";
    # Lookup the results for all scoring schemes
    print $all_real{$key}->{"first"} . "\n";
    print $all_real{$key}->{"median"}. "\n";
    print $all_real{$key}->{"last"}  . "\n";
}
close(IN);
