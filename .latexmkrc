# Main file
@default_files = ('REPORT.TEX');

# Use pdflatex
$pdf_mode = 1;
$pdflatex = 'pdflatex -interaction=nonstopmode -synctex=1 %O %S';

# BibTeX settings
$bibtex_use = 2;   # run bibtex even if bib files don't appear changed
$bibtex = 'bibtex %O %B';

# Keep aux files alongside the main .tex file (Disertation/ root)
$out_dir = '.';

# BIBINPUTS: look in same dir as REPORT.TEX
ensure_path('BIBINPUTS', '.');
ensure_path('BSTINPUTS', '.');
