Scripts to consolidate, clean up, and fuss with data.

A common workflow: you've used the `analysis/pipeline-scripts` scripts to generate one or more score files, and you want to combine that new data with a pre-existing score file.

To do this:

- Use `amend-scores` to graft the new scores onto the existing file.
- Use `clean_up_scores.pl` to ensure that every fault still has the right number of scores (for example, `data/scores_artificial_vs_real.csv` should have 7 scores per fault, one per previously studied FL technique; `data/scores_real_exploration.csv` should have 2384, one per debugging scenario per technique studied in the paper).

The other scripts here are utilities used by the statistical-analysis scripts.
