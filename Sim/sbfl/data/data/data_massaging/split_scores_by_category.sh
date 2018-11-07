#!/bin/bash

BUGGY_LINES_DIR=$1
SCORES_FILE=$2
OUTPUT_DIR=$3

MO_PATTERN=''
M_PATTERN=''
SO_PATTERN=''
S_PATTERN=''

for f in $BUGGY_LINES_DIR/*.buggy.lines; do
  PROJECT=$(basename "$f" | cut -d - -f 1)
  BUG=$(sed 's/.*-\(.*\).buggy.lines/\1/' <<< $f)
  if [ "$BUG" -gt 1000 ]; then continue; fi

  MULTI=$(if [ $(wc -l < $f) -gt 1 ]; then echo yes; else echo no; fi)
  OMISSION=$(if grep -q FAULT_OF_OMISSION $f; then echo yes; else echo no; fi)

  case "$MULTI $OMISSION" in
    ('yes yes') MO_PATTERN="$MO_PATTERN|$PROJECT,$BUG" ;;
    ('yes no')  M_PATTERN="$M_PATTERN|$PROJECT,$BUG" ;;
    ('no yes')  SO_PATTERN="$SO_PATTERN|$PROJECT,$BUG" ;;
    ('no no')   S_PATTERN="$S_PATTERN|$PROJECT,$BUG" ;;
  esac
done

# Avoid compatibility issues and remove the first empty pattern
MO_PATTERN=$(sed 's/^|//' <<< $MO_PATTERN)
if [ "$MO_PATTERN" ]; then
  RELEVANT_BUG_PATTERN="^($MO_PATTERN)([0-9]{5})?,"
  OUTPUT_FILE="$OUTPUT_DIR/scores_for_multiline_with_omission.csv"
  head -n 1 "$SCORES_FILE" > "$OUTPUT_FILE"
  egrep "$RELEVANT_BUG_PATTERN" "$SCORES_FILE" >> "$OUTPUT_FILE"
fi

# Avoid compatibility issues and remove the first empty pattern
M_PATTERN=$(sed 's/^|//' <<< $M_PATTERN)
if [ "$M_PATTERN" ]; then
  RELEVANT_BUG_PATTERN="^($M_PATTERN)([0-9]{5})?,"
  OUTPUT_FILE="$OUTPUT_DIR/scores_for_multiline_without_omission.csv"
  head -n 1 "$SCORES_FILE" > "$OUTPUT_FILE"
  egrep "$RELEVANT_BUG_PATTERN" "$SCORES_FILE" >> "$OUTPUT_FILE"
fi

# Avoid compatibility issues and remove the first empty pattern
SO_PATTERN=$(sed 's/^|//' <<< $SO_PATTERN)
if [ "$SO_PATTERN" ]; then
  RELEVANT_BUG_PATTERN="^($SO_PATTERN)([0-9]{5})?,"
  OUTPUT_FILE="$OUTPUT_DIR/scores_for_single_line_with_omission.csv"
  head -n 1 "$SCORES_FILE" > "$OUTPUT_FILE"
  egrep "$RELEVANT_BUG_PATTERN" "$SCORES_FILE" >> "$OUTPUT_FILE"
fi

# Avoid compatibility issues and remove the first empty pattern
S_PATTERN=$(sed 's/^|//' <<< $S_PATTERN)
if [ "$S_PATTERN" ]; then
  RELEVANT_BUG_PATTERN="^($S_PATTERN)([0-9]{5})?,"
  OUTPUT_FILE="$OUTPUT_DIR/scores_for_single_line_without_omission.csv"
  head -n 1 "$SCORES_FILE" > "$OUTPUT_FILE"
  egrep "$RELEVANT_BUG_PATTERN" "$SCORES_FILE" >> "$OUTPUT_FILE"
fi
